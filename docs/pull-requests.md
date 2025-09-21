# Pull Request and Code Review Guidelines

Back to [Main page](../README.md)

<!-- TOC -->
* [Pull Request and Code Review Guidelines](#pull-request-and-code-review-guidelines)
  * [Pull Request Guidelines](#pull-request-guidelines)
    * [Pull Requests (PR) Naming](#pull-requests-pr-naming)
    * [Pull Request Commit Naming](#pull-request-commit-naming)
  * [Code Review Guidelines](#code-review-guidelines)
    * [Frontend Checklist](#frontend-checklist)
    * [Backend Checklist](#backend-checklist)
    * [General Checklist](#general-checklist)
<!-- TOC -->

## Pull Request Guidelines

### Pull Requests (PR) Naming

- To make it so we can easily search and find pull requests we will adhere to the following standard:

```
feat(TEAMTAG-JiraID): short description
```

- In that example, you would replace TEAMTAG with your team's acronym and the JIRA-TICKET-ID with the id from Jira.
- Keep the parentheses.
- Do not include any capital letters or punctuation in the description

### Pull Request Commit Naming

- This is pretty much the exact same as the Pull Request Naming except that at the end there will be an auto-generated number in parentheses. Please don't delete it. Simply add your stuff before it.

```
feat(TEAMTAG-JiraID): short description (#420)
```

## Code Review Guidelines

### Frontend Checklist

- [ ] Uses shared axios instance instead of fetch() or new axios instances
- [ ] No hardcoded URLs - uses environment variables
- [ ] Always explicitly specifies API version using `useV2` flag for clarity
- [ ] Prefers v1 API (`{ useV2: false }`) unless breaking changes require v2
- [ ] Only uses v2 API (`{ useV2: true }`) when non-backwards compatible changes are needed
- [ ] Proper TypeScript types and interfaces
- [ ] Includes error handling with try-catch
- [ ] Follows feature-based folder structure
- [ ] Uses proper React hooks and patterns
- [ ] No manual URL concatenation with baseURL
- [ ] External URLs (http://, https://) are not affected by versioning system


### Backend Checklist

- [ ] Uses v1 API endpoints for new features unless breaking changes are required
- [ ] Only uses v2 API endpoints when non-backwards compatible changes are needed
- [ ] Implements proper security annotations
- [ ] Uses reactive patterns (Mono/Flux) consistently
- [ ] Includes proper error handling and custom exceptions
- [ ] Follows RESTful naming conventions
- [ ] Uses WebClient for service-to-service communication
- [ ] Includes proper validation on request DTOs
- [ ] Uses consistent response patterns

### General Checklist

- [ ] Code follows established patterns in the codebase
- [ ] Includes appropriate logging
- [ ] Has proper unit/integration tests
- [ ] Documentation is updated if needed
- [ ] No breaking changes to existing APIs
- [ ] Performance considerations addressed
- [ ] Security implications reviewed