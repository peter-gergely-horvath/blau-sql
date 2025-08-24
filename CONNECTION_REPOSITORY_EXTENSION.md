# Custom Connection Definition Repository

BlauSQL now supports custom implementations of the `ConnectionDefinitionRepository` interface, allowing you to store connection definitions in any way you choose.

## Creating a Custom Implementation

1. Implement the `ConnectionDefinitionRepository` interface:

```java
package com.example.myapp.connection;

import com.github.blausql.spi.connections.ConnectionDefinitionRepository;
import com.github.blausql.core.connection.ConnectionDefinition;

public class MyCustomRepository implements ConnectionDefinitionRepository {
    @Override
    public List<ConnectionDefinition> getConnectionDefinitions() {
        // Your implementation here
    }

    @Override
    public void saveConnectionDefinition(ConnectionDefinition connectionDefinition) {
        // Your implementation here
    }

    @Override
    public void deleteConnectionDefinitionByName(String connectionName) {
        // Your implementation here
    }
}
```

2. Create a provider class that implements `ConnectionDefinitionRepositoryProvider`:

```java
package com.example.myapp.connection;

import com.github.blausql.spi.connections.ConnectionDefinitionRepository;
import com.github.blausql.spi.connections.ConnectionDefinitionRepositoryProvider;

public class MyCustomRepositoryProvider implements ConnectionDefinitionRepositoryProvider {
    @Override
    public int getPriority() {
        // Use a priority higher than 0 to override the default implementation
        return 10;
    }

    @Override
    public ConnectionDefinitionRepository createRepository() {
        return new MyCustomRepository();
    }
}
```

3. Register your provider by creating a service registration file:

Create a file at `META-INF/services/com.github.blausql.spi.connections.ConnectionDefinitionRepositoryProvider` with the following content:

```
com.example.myapp.connection.MyCustomRepositoryProvider
```

## Build Configuration

Make sure your implementation is on the classpath when running BlauSQL. The application will automatically detect and use your implementation if it has a higher priority than the default implementation.

## Example: Database-Backed Repository

Here's a simple example of a repository that stores connections in a database:

```java
public class DatabaseConnectionRepository implements ConnectionDefinitionRepository {
    private final DataSource dataSource;

    public DatabaseConnectionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ConnectionDefinition> getConnectionDefinitions() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM connections");
             ResultSet rs = stmt.executeQuery()) {
            
            List<ConnectionDefinition> connections = new ArrayList<>();
            while (rs.next()) {
                ConnectionDefinition cd = new ConnectionDefinition(rs.getString("name"));
                cd.setDriverClassName(rs.getString("driver_class"));
                cd.setJdbcUrl(rs.getString("jdbc_url"));
                cd.setUserName(rs.getString("username"));
                cd.setPassword(rs.getString("password"));
                cd.setStatementSeparator(rs.getBoolean("statement_separator"));
                cd.setLoginAutomatically(rs.getBoolean("login_automatically"));
                cd.setHotkey(rs.getString("hotkey").charAt(0));
                cd.setOrder(rs.getInt("sort_order"));
                connections.add(cd);
            }
            return connections;
        } catch (SQLException e) {
            throw new LoadException("Failed to load connections", e);
        }
    }

    // Implement other methods...
}
```

## Notes

- The provider with the highest priority will be used
- The default implementation has a priority of 0
- Your implementation should be thread-safe as it may be accessed from multiple threads
- Make sure to properly handle exceptions and document any custom exceptions your implementation may throw
