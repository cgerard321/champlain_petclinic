use sqlx::{MySql, Pool};
use std::collections::HashSet;
use std::sync::Arc;
use uuid::fmt::Hyphenated;
use uuid::Uuid;

use crate::adapters::output::mysql::error::map_sqlx_err;
use crate::adapters::output::mysql::model::role::Role;
use crate::adapters::output::mysql::model::user::User;
use crate::application::ports::output::user_repo_port::UsersRepoPort;
use crate::application::services::auth::projections::AuthProjection;
use crate::domain::entities::user::{RoleEntity, UserEntity};
use crate::shared::error::{AppError, AppResult};

pub struct MySqlUsersRepo {
    pool: Arc<Pool<MySql>>,
}

impl MySqlUsersRepo {
    pub fn new(pool: Arc<Pool<MySql>>) -> Self {
        Self { pool }
    }
}

#[async_trait::async_trait]
impl UsersRepoPort for MySqlUsersRepo {
    async fn insert_user_hashed(
        &self,
        id: Uuid,
        email: &str,
        pass_hash: &[u8],
        display_name: &str,
        user_roles: HashSet<Uuid>,
    ) -> AppResult<UserEntity> {
        if user_roles.is_empty() {
            return Err(AppError::BadRequest(
                "At least one role must be provided".to_string(),
            ));
        }

        log::info!("Inserting user: {:?}", id);
        let mut tx = self
            .pool
            .begin()
            .await
            .map_err(|e| map_sqlx_err(e, "User"))?;

        let user_id = Hyphenated::from_uuid(id).to_string();

        sqlx::query(
            r#"
            INSERT INTO users (id, email, pass_hash, display_name)
            VALUES (?, ?, ?, ?)
            "#,
        )
            .bind(&user_id)
            .bind(email)
            .bind(pass_hash)
            .bind(display_name)
            .execute(&mut *tx)
            .await
            .map_err(|e| map_sqlx_err(e, "User"))?;

        for role_id in user_roles {
            let rid_str = role_id.as_hyphenated().to_string();
            log::info!("Inserting role: {} for user: {}", rid_str, user_id);

            if let Err(e) = sqlx::query(
                r#"
        INSERT INTO user_roles (user_id, role_id)
        VALUES (?, ?)
        "#,
            )
                .bind(&user_id)
                .bind(&rid_str)
                .execute(&mut *tx)
                .await
            {
                tx.rollback().await.map_err(|e| map_sqlx_err(e, "Role"))?;
                return Err(map_sqlx_err(e, "Role"));
            }
        }

        tx.commit().await.map_err(|e| map_sqlx_err(e, "User"))?;

        Ok(self.get_user_by_id(id).await?)
    }
    async fn get_user_by_id(&self, id: Uuid) -> AppResult<UserEntity> {
        log::info!("Finding user: {:?}", id);
        let id = Hyphenated::from_uuid(id);

        let row: User = sqlx::query_as::<_, User>(
            r#"
        SELECT id, email, display_name, is_active, pass_hash
        FROM users
        WHERE id = ?
        "#,
        )
            .bind(id)
            .fetch_one(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "User"))?;

        log::info!("User found: {:?}", row);

        let mut user = UserEntity::from(row);

        let roles = self.get_roles_for_user(user.user_id).await?;

        user.roles = roles;

        Ok(user)
    }

    async fn get_user_auth_by_email_for_login(&self, email: &str) -> AppResult<AuthProjection> {
        log::info!("Finding user for login: {:}", email);
        let row: User = sqlx::query_as::<_, User>(
            r#"
        SELECT id, email, display_name, is_active, pass_hash
        FROM users
        WHERE email = ?
        "#,
        )
            .bind(email)
            .fetch_one(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "User"))?;

        log::info!("User found: {:?}", row);

        Ok(AuthProjection::try_from(row)?)
    }

    async fn get_roles_for_user(&self, user_id: Uuid) -> AppResult<HashSet<RoleEntity>> {
        let user_id = Hyphenated::from_uuid(user_id).to_string();

        let rows: Vec<Role> = sqlx::query_as::<_, Role>(
            r#"
            SELECT r.id as id, r.code, r.description
            FROM roles r
            JOIN user_roles ur ON ur.role_id = r.id
            WHERE ur.user_id = ?
            "#,
        )
            .bind(user_id)
            .fetch_all(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Role"))?;

        let roles = rows.into_iter().map(RoleEntity::from);

        Ok(roles.collect())
    }
}
