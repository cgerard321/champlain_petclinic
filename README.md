# Champlain Pet Clinic

Champlain Final Project 1 420-N52-LA Pet Clinic repo

* [Champlain Pet Clinic](#champlain-pet-clinic)
  * [Source](#source)
  * [Getting Started](#getting-started)
  * [Questions or Issues?](#questions-or-issues)
  * [Development Notes](#development-notes)
* [Pull Request Guidelines](docs/pull-requests.md)
* [Git Guidelines](docs/git-tips.md)
* [React Coding Standards](docs/react-coding-standards.md)
* [Java (Spring) Coding Standards](docs/java-coding-standards.md)
* [API Versioning Guidelines](docs/api-versioning.md)
* [Scrum and Agile Guidelines](docs/scrum-agile.md)
* [Environment Setup](docs/environment.md)
* [Running the Application with Docker](docs/running-project.md)
* [Spring Security Overview](docs/spring-security.md)


## Source
This project is based on the spring petclinic microservices (https://github.com/spring-petclinic/spring-petclinic-microservices) implementation.
However, only the customers, visits, vets, and api-gateway services have been retained. In addition, the 
Docker setup has been changed.

## Getting Started

1. **Setup Environment**: Copy the appropriate [environment](docs/environment.md) file and configure your variables and [run it](docs/running-project.md)
2. **Review Existing Code**: Study the patterns in `/src/shared/api/axiosInstance.ts` and similar files
3. **Follow the Standards**: Use the examples in these : [React Coding Standards](docs/react-coding-standards.md), [Backend Coding Standards](docs/java-coding-standards.md), [Git guidelines](docs/git-tips.md) and [Pull Request Guidelines](docs/pull-requests.md)
4. **Test Thoroughly**: Ensure your changes work with the existing infrastructure
5. **Submit for Review**: Include a checklist in your PR description

## Questions or Issues?

If you have questions about these standards or need clarification on any patterns, please:

1. Check existing code examples in the repository
2. Review this contributing guide
3. Ask questions in pull request discussions
4. Reach out to the team leads for architectural decisions

---

**Remember**: These standards ensure consistency, maintainability, and reliability across the entire codebase. Following them helps everyone and makes the application more robust.


## Development Notes
If there's tests that are commented out, they need to be fixed. They were likely causing issues with the CI/CD pipeline.

## C4 L2
![C4 L2](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/cgerard321/champlain_petclinic/main/docs/diagrams/C4/champlain-pet-clinic-ms_C4_L2_container_diagram.puml&fmt=svg)
