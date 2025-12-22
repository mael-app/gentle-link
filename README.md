# GentleLink

A Minecraft Paper 1.21 plugin to link Minecraft and Discord accounts.

## Features

- 🔗 Minecraft-Discord account linking via 6-character code
- ⏱️ Temporary codes (5-minute expiration)
- 🔒 Rate limiting to prevent spam (30 seconds between each generation)
- 🚫 **Player restrictions for unlinked accounts** (blindness, movement blocking)
- 💬 **Customizable messages** (chat, title, subtitle) using MiniMessage format
- 👥 Administrative management (list, bypass, info)
- 🎨 Elegant and interactive chat interface
- 💾 SQLite and MariaDB support
- 🔐 Granular permission system

## Installation

1. Download the JAR file from [releases](https://github.com/mael-app/gentle-link/releases)
2. Place it in the `plugins/` folder of your Paper 1.21 server
3. Restart the server
4. Configure the plugin in `plugins/GentleLink/config.yml`

## Configuration

### Database

By default, the plugin uses SQLite. To use MariaDB:

```yaml
database:
  type: MARIADB
  
  mariadb:
    enabled: true
    host: localhost
    port: 3306
    database: gentlelink
    username: your_username
    password: your_secure_password
```

**⚠️ IMPORTANT: Security**
- NEVER share your `config.yml` file with sensitive information
- Use strong passwords for your database
- Limit the database user's permissions to only the necessary tables

### Messages Customization

Customize messages shown to unlinked players using [MiniMessage](https://docs.advntr.dev/minimessage/format.html) format:

```yaml
messages:
  not_linked:
    chat: |
      <gold><bold>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</bold></gold>
      <yellow><bold>⚠ Account Not Linked</bold></yellow>
      ...
    title: "<red><bold>⚠ Account Not Linked</bold></red>"
    subtitle: "<yellow>Use <green>/link</green> to connect</yellow>"
```

## Commands

### Players
- `/link` - Generate a 6-character linking code
- `/link info` - Display your linking information
- `/unlink` - Remove your account link

### Administration
- `/link list` - List all linked accounts
- `/link info <username>` - Display a player's linking information
- `/link bypass <username> <discord_id>` - Create a manual link
- `/unlink <username>` - Unlink another player's account

## Permissions

### Basic permissions (granted by default)
- `gentlelink.link` - Generate a linking code
- `gentlelink.link.info` - View your own information
- `gentlelink.unlink` - Unlink your account

### Administrator permissions (OP by default)
- `gentlelink.link.info.others` - View others' information
- `gentlelink.link.list` - List all linked accounts
- `gentlelink.link.bypass` - Create manual links
- `gentlelink.unlink.others` - Unlink other players' accounts
- `gentlelink.admin` - Full access (groups all admin permissions)

## Security

This plugin implements several security measures:

### Input Validation
- Discord IDs must be 17 to 19 digits
- Linking codes must be alphanumeric uppercase (6 characters)
- Player names are validated according to Minecraft standards

### Rate Limiting
- 30 seconds minimum between each code generation per player
- Prevents spam and bruteforce attempts

### Error Handling
- Appropriate logging of all SQL errors
- Informative error messages without exposing sensitive details
- Custom exceptions for better management

### Database
- Use of PreparedStatements (SQL injection prevention)
- Connection management via HikariCP (efficient pooling)
- Support for SGBD-specific UPSERT syntaxes

## Development

### Prerequisites
- Java 21
- Maven 3.x
- Paper 1.21 server

### Compilation
```bash
mvn clean package
```

The JAR file will be generated in `target/GentleLink-1.0-SNAPSHOT.jar`

### Architecture

The project follows SOLID principles:
- **Single Responsibility**: Each class has a single responsibility
- **Open/Closed**: Extensible via interfaces and abstract classes
- **Liskov Substitution**: Implementations respect contracts
- **Interface Segregation**: Specific and targeted interfaces
- **Dependency Inversion**: Dependencies on abstractions

#### Project Structure
```
src/main/java/app/mael/gentleLink/
├── command/           # Plugin commands
├── database/          # Data access layer
├── exception/         # Custom exceptions
├── model/             # Data models (records)
├── permission/        # Centralized permission management
├── service/           # Business logic
├── util/             # Utilities (validation)
└── Main.java         # Main plugin class
```

## Contributing

Contributions are welcome! To contribute:

1. Fork the project
2. Create a branch for your feature (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## Support

For questions or issues:
- Open an [issue](https://github.com/mael-app/gentle-link/issues)

---

Made with ❤️ by [Maël](https://github.com/mael-app)

