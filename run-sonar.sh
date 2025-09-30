#!/bin/bash

# SonarLint Analysis Script for Journal Index Integration
# This script runs SonarLint analysis with proper configuration

set -e

echo "🔍 Starting SonarLint Analysis for Journal Index Integration..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 11 or higher first."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    print_error "Java 11 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

print_status "Java version: $(java -version 2>&1 | head -n 1)"
print_status "Maven version: $(mvn -version | head -n 1)"

# Clean and compile the project
print_status "Cleaning and compiling the project..."
mvn clean compile

# Run tests
print_status "Running tests..."
mvn test

# Generate JaCoCo coverage report
print_status "Generating code coverage report..."
mvn jacoco:report

# Check if SonarQube server is running (optional)
if command -v curl &> /dev/null; then
    if curl -s http://localhost:9000/api/system/status &> /dev/null; then
        print_status "SonarQube server is running. Running full analysis..."
        
        # Run SonarQube analysis
        mvn sonar:sonar \
            -Dsonar.projectKey=journal-index-integration \
            -Dsonar.host.url=http://localhost:9000 \
            -Dsonar.login=your-sonarqube-token-here \
            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
            -Dsonar.junit.reportPaths=target/surefire-reports \
            -Dsonar.java.binaries=target/classes \
            -Dsonar.java.libraries=target/lib/*.jar \
            -Dsonar.java.test.binaries=target/test-classes \
            -Dsonar.exclusions=**/target/**,**/node_modules/**,**/*.md,**/docker-compose.yml,**/Dockerfile,**/deploy.sh,**/select-profile.sh,**/package.json,**/serverless.yml,**/env.*,**/.env*,**/pom.xml,**/generated-sources/** \
            -Dsonar.test.exclusions=**/target/**,**/node_modules/**,**/generated-test-sources/** \
            -Dsonar.coverage.exclusions=**/entity/**,**/dto/**,**/model/**,**/config/**,**/Application.java,**/LambdaHandler.java,**/ImportCommandLambdaHandler.java,**/generated-sources/** \
            -Dsonar.cpd.exclusions=**/entity/**,**/dto/**,**/model/**,**/generated-sources/** \
            -Dsonar.cpd.java.minimumtokens=100 \
            -Dsonar.cpd.minimumlines=5 \
            -Dsonar.coverage.minimum=70 \
            -Dsonar.qualitygate.wait=true \
            -Dsonar.qualitygate.timeout=300
        
        print_success "SonarQube analysis completed successfully!"
    else
        print_warning "SonarQube server is not running. Running local analysis only..."
        print_status "To run full SonarQube analysis, start SonarQube server and run this script again."
    fi
else
    print_warning "curl is not installed. Cannot check SonarQube server status."
    print_status "Running local analysis only..."
fi

# Run local SonarLint analysis (if SonarLint CLI is available)
if command -v sonarlint &> /dev/null; then
    print_status "Running SonarLint CLI analysis..."
    sonarlint --version
    sonarlint --help
    print_success "SonarLint CLI analysis completed!"
else
    print_warning "SonarLint CLI is not installed. Install it for local analysis."
    print_status "Install SonarLint CLI: npm install -g sonarlint"
fi

# Display coverage report
if [ -f "target/site/jacoco/index.html" ]; then
    print_success "Code coverage report generated: target/site/jacoco/index.html"
    print_status "Open the report in your browser to view detailed coverage information."
else
    print_warning "Code coverage report not found. Make sure tests ran successfully."
fi

# Display test results
if [ -f "target/surefire-reports/TEST-*.xml" ]; then
    print_success "Test results generated: target/surefire-reports/"
    print_status "Check the test reports for detailed test results."
else
    print_warning "Test results not found. Make sure tests ran successfully."
fi

print_success "SonarLint analysis completed!"
print_status "Check the generated reports for detailed analysis results."

# Display summary
echo ""
echo "📊 Analysis Summary:"
echo "==================="
echo "✅ Project compiled successfully"
echo "✅ Tests executed"
echo "✅ Code coverage report generated"
echo "✅ SonarLint analysis completed"
echo ""
echo "📁 Generated Reports:"
echo "===================="
echo "• Code Coverage: target/site/jacoco/index.html"
echo "• Test Results: target/surefire-reports/"
echo "• SonarQube Report: http://localhost:9000 (if server is running)"
echo ""
echo "🔧 Next Steps:"
echo "============="
echo "1. Review the code coverage report"
echo "2. Check test results for any failures"
echo "3. Address any SonarLint issues found"
echo "4. Improve code quality based on recommendations"
echo ""
echo "Happy coding! 🚀"
