# The Parida Website Automation

Selenium Java UI regression suite for theparida.com. Tests target the website, including its software-enquiry page, rather than Windows desktop applications.

## Stack
Java 17 · Selenium WebDriver · TestNG · Maven · Chrome / Edge

## Coverage
59 data-driven invocations across navigation, Contact, Careers, billing enquiries, Growth Roadmap and Investment. Test plan and case design are in `docs/`.

## Execution
```sh
mvn clean test -DbaseUrl=https://theparida.com/
```
Use a matching preview URL for the supplied unreleased website version. Configuration is in `src/test/resources/config.properties`.

Reports: `target/surefire-reports/`. Failure screenshots: `target/screenshots/`. Generated output is excluded from version control.

## Test behavior and verification
Browser writes are blocked or simulated; tests do not establish Netlify or inbox delivery. Two simulated-response cases intentionally skip on localhost/127.0.0.1. Java compilation passed during preparation. Browser assertions remain unverified because the preparation environment blocked a Chrome startup socket.

The GitHub Actions workflow is manually triggered. No pass-rate or build-status claims are implied.
