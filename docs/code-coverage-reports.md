# Code Coverage Reports Access Guide

Back to [Main page](../README.md)

<!-- TOC -->

- [Code Coverage Reports Access Guide](#code-coverage-reports-access-guide)
  - [Overview](#overview)
  - [Where to Find Coverage Reports](#where-to-find-coverage-reports)
    - [GitHub Actions Artifacts](#github-actions-artifacts)
    - [Pull Request Coverage Summary](#pull-request-coverage-summary)
    - [Local Development](#local-development)
  - [Understanding Coverage Thresholds](#understanding-coverage-thresholds)
  - [How to Read Coverage Reports](#how-to-read-coverage-reports)
    - [HTML Report Navigation](#html-report-navigation)
    - [Coverage Metrics Explained](#coverage-metrics-explained)
  - [Troubleshooting](#troubleshooting)
  <!-- TOC -->

## Overview

The Champlain Pet Clinic project uses JaCoCo (Java Code Coverage) to track test coverage across all microservices. Coverage reports are automatically generated during the CI/CD pipeline execution and are available through multiple channels to help developers monitor test quality and meet coverage requirements.

## Where to Find Coverage Reports

### GitHub Actions Artifacts

**Location:** GitHub Actions → Workflow Runs → Artifacts

1. Navigate to the [GitHub Actions page](https://github.com/cgerard321/champlain_petclinic/actions)
2. Click on any completed workflow run (green checkmark)
3. Scroll down to the "Artifacts" section
4. Download the coverage report for your service:
   - `coverage-reports-api-gateway`
   - `coverage-reports-auth-service`
   - `coverage-reports-billing-service`
   - `coverage-reports-cart-service`
   - `coverage-reports-customers-service`
   - `coverage-reports-inventory-service`
   - `coverage-reports-products-service`
   - `coverage-reports-visits-service`

**How to View:**

1. Download the artifact ZIP file
2. Extract the contents
3. Open `index.html` in your web browser
4. Navigate through the interactive coverage report

### Pull Request Coverage Summary

**Location:** Pull Request → Checks → Coverage Summary

When you create a pull request, the CI/CD pipeline automatically generates a coverage summary that appears in the PR checks section. This summary includes:

- Overall coverage status for all microservices
- Coverage thresholds for each service
- Links to detailed reports in artifacts
- Pass/fail status based on coverage requirements

**How to Access:**

1. Open your pull request
2. Scroll down to the "Checks" section
3. Look for the "coverage-summary" job
4. Click to expand and view the summary

## Understanding Coverage Thresholds

Each microservice has specific coverage requirements that must be met for the CI/CD pipeline to pass (they all should reach 90%):

| Service           | Team Responsibility             |
| ----------------- | ------------------------------- |
| API Gateway       | All Teams (Shared Responsibility) |
| Auth Service      | Auth Team                      |
| Billing Service   | Billing Team                   |
| Cart Service      | Cart Team                      |
| Customers Service | Customers Team                 |
| Inventory Service | Inventory Team                 |
| Products Service  | Products Team                  |
| Vet Service       | Vet Team                       |
| Visits Service    | Visits Team                    |

**Important Notes:**

- **Target Goal:** All services must reach 90% coverage
- **API Gateway:** All teams share responsibility for this service
- **Other Services:** Each team is responsible for their own service
- **CI/CD Enforcement:** All thresholds are enforced during CI/CD pipeline execution
- **Build Failure:** Failing to meet thresholds will cause the build to fail
- **Continuous Improvement:** Teams should continuously work on increasing test coverage

## How to Read Coverage Reports

### HTML Report Navigation

The JaCoCo HTML report provides a comprehensive view of code coverage:

1. **Overview Page (`index.html`):**

   - Total coverage percentage
   - Coverage by package
   - Coverage by class
   - Coverage by method

2. **Package View:**

   - Click on any package 
   - See coverage for all classes in the package
   - Identify which classes need more testing

3. **Class View:**

   - Line-by-line coverage visualization
   - Green lines: covered by tests
   - Red lines: not covered by tests
   - Yellow lines: partially covered

4. **Source Code View:**
   - Click on any class to see the actual source code
   - Lines are color-coded based on coverage
   - Hover over lines to see coverage details

### Coverage Metrics Explained

- **Line Coverage:** Percentage of executable lines covered by tests
- **Branch Coverage:** Percentage of conditional branches covered by tests
- **Method Coverage:** Percentage of methods called by tests
- **Class Coverage:** Percentage of classes with at least one method covered





**: Coverage reports not appearing in artifacts**

- Ensure the workflow completed successfully
- Check that `jacocoTestReport` task ran without errors
- Verify the service has test files

**: Coverage threshold failures**

- Review the coverage report to identify uncovered code
- Add tests for missing coverage areas
- Consider if the threshold is appropriate for the service



**: Cannot access HTML reports**

- Try downloading artifacts from a different browser
- Check if the artifact has expired (30-day retention)

### Getting Help

If you encounter issues with coverage reports:

1. Check the [GitHub Actions logs](https://github.com/cgerard321/champlain_petclinic/actions)
2. Review the [Java Coding Standards](java-coding-standards.md) 
3. Refer to the [JaCoCo documentation](https://www.jacoco.org/jacoco/trunk/doc/) 

---

**Related Documentation:**

- [Java Coding Standards](java-coding-standards.md)
- [Pull Request Guidelines](pull-requests.md)
- [CI/CD Pipeline Overview](../.github/workflows/parallel_microservice_build.yml)
