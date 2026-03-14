# Database Backup Utility

A comprehensive command-line interface (CLI) utility for backing up multiple database types with support for various cloud storage solutions.

## Features

- **Multi-DBMS Support**: MySQL, PostgreSQL, MongoDB, SQLite, and more
- **Backup Types**: Full, incremental, and differential backups
- **Compression**: Built-in compression support for backup files
- **Cloud Storage**: Integration with AWS S3, Azure Blob Storage, and Google Cloud Storage
- **Local Storage**: Direct backup to local file system
- **Restore Operations**: Flexible database restoration with selective restore capability
- **Logging**: Comprehensive activity logging with rotating file support
- **Notifications**: Slack notifications on backup completion
- **Connection Testing**: Validate database credentials before backup
- **Error Handling**: Robust error handling and recovery mechanisms

## Project Structure

```
database_backup_utility/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/databasebackup/
│   │   │       ├── cli/              - CLI commands and entry point
│   │   │       ├── database/         - Database connectivity (Phase 2)
│   │   │       ├── backup/           - Backup operations (Phase 3)
│   │   │       ├── restore/          - Restore operations (Phase 4)
│   │   │       ├── storage/          - Storage backends (Phase 5)
│   │   │       ├── compression/      - Compression utilities (Phase 5)
│   │   │       ├── notifications/    - Notification handlers (Phase 6)
│   │   │       └── utils/            - Utility functions
│   │   └── resources/
│   │       └── logback.xml           - Logging configuration
│   └── test/
│       ├── java/
│       └── resources/
├── pom.xml                            - Maven configuration
├── README.md                          - This file
└── .gitignore                         - Git ignore rules
```

## Technology Stack

- **Language**: Java 11+
- **Build Tool**: Maven
- **CLI Framework**: PicoCLI
- **Logging**: SLF4J + Logback
- **Database Drivers**: 
  - MySQL Connector/J
  - PostgreSQL JDBC
  - MongoDB Java Driver
- **Cloud SDKs**:
  - AWS SDK for Java
  - Azure SDK for Java
  - Google Cloud Storage Java Client

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven 3.6+
- Database access (MySQL, PostgreSQL, MongoDB, etc.)
- (Optional) Cloud credentials for backup storage

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd database_backup_utility
```

2. Build the project:
```bash
mvn clean install
```

3. Create the executable JAR:
```bash
mvn clean package
```

The compiled JAR will be created at: `target/db-backup-1.0.0-SNAPSHOT.jar`

### Running the Application

```bash
# Display help
java -jar target/db-backup-1.0.0-SNAPSHOT.jar --help

# Display version
java -jar target/db-backup-1.0.0-SNAPSHOT.jar --version
```

## Usage Examples

(Examples will be added as features are implemented in subsequent commits)

### Backup Operations

```bash
# Full backup of MySQL database
java -jar db-backup.jar backup \
  --type=mysql \
  --host=localhost \
  --port=3306 \
  --username=root \
  --password=password \
  --database=mydb \
  --backup-type=full \
  --compression=gzip \
  --output=/backups

# Backup to AWS S3
java -jar db-backup.jar backup \
  --type=postgresql \
  --host=db.example.com \
  --database=production \
  --storage=s3 \
  --s3-bucket=my-backups \
  --s3-region=us-east-1
```

### Restore Operations

```bash
# Restore from local backup
java -jar db-backup.jar restore \
  --type=mysql \
  --host=localhost \
  --database=mydb \
  --backup-file=/backups/mydb-2024-03-14.sql.gz

# Selective restore (restore specific tables)
java -jar db-backup.jar restore \
  --type=mysql \
  --host=localhost \
  --database=mydb \
  --backup-file=/backups/mydb-2024-03-14.sql.gz \
  --tables=users,orders
```

## Configuration

Configuration can be provided via:
- Command-line arguments
- Configuration file (`.properties` or `.yaml`)
- Environment variables

## Logging

Logs are written to:
- **Console**: Real-time information and errors
- **File**: `./logs/db-backup.log` with rotation

Log levels can be configured in `src/main/resources/logback.xml`

## Development Roadmap

### Phase 1: Project Setup ✓
- Maven project configuration
- CLI framework setup
- Logging infrastructure

### Phase 2: Database Connection Framework
- Connection pooling
- Driver management
- Connection testing utilities

### Phase 3: Individual Database Support
- MySQL implementation
- PostgreSQL implementation
- MongoDB implementation

### Phase 4: Backup Operations
- Full backup implementation
- Incremental backup
- Differential backup

### Phase 5: Storage & Compression
- Local storage
- Cloud storage integration (S3, Azure, GCS)
- Compression utilities

### Phase 6: Restore & Notifications
- Restore operations
- Selective restore
- Slack/Email notifications
- Activity logging

## Testing

Run unit tests:
```bash
mvn test
```

Run specific test:
```bash
mvn test -Dtest=TestClassName
```

## Security Considerations

- Database credentials are handled securely
- Support for encrypted connection strings
- Cloud credentials use environment variables or secure stores
- Backup files are encrypted before storage when possible
- No sensitive data logged

## Performance

- Efficient handling of large databases
- Streaming-based backup for memory efficiency
- Parallel processing support for large backup operations
- Connection pooling for database efficiency

## Error Handling

The utility includes comprehensive error handling for:
- Database connection failures
- Network interruptions during cloud uploads
- Insufficient disk/cloud storage space
- Invalid backup/restore parameters
- Database-specific errors

## Troubleshooting

(Troubleshooting guide will be expanded as features are implemented)

### Common Issues

1. **Connection timeouts**: Check database host and network connectivity
2. **Authentication failures**: Verify credentials and user permissions
3. **Cloud upload failures**: Check cloud credentials and bucket permissions

## Contributing

1. Create a feature branch
2. Make your changes
3. Add tests for new features
4. Submit a pull request

## License

(To be specified)

## Support

(To be added)

## Changelog

### Version 1.0.0-SNAPSHOT
- Initial project setup
- CLI framework established
- Logging infrastructure configured
