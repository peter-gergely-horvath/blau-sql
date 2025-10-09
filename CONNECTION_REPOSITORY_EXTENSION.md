# Connection Repository Extension

BlauSQL now supports custom implementations of the `ConnectionConfigurationRepository` interface, allowing you to store connection configurations in any way you choose.

## Creating a Custom Repository

1. Implement the `ConnectionConfigurationRepository` interface:

```java
package com.example;

import com.github.blausql.spi.connections.ConnectionConfigurationRepository;
import com.github.blausql.core.connection.ConnectionConfiguration;

public class MyCustomRepository implements ConnectionConfigurationRepository {
    @Override
    public List<ConnectionConfiguration> getConnectionConfigurations() {
        // Your implementation here
        return Collections.emptyList();
    }

    @Override
    public void saveConnectionConfiguration(ConnectionConfiguration connectionConfiguration) {
        // Your implementation here
    }

    @Override
    public void deleteConnectionConfigurationByName(String connectionName) {
        // Your implementation here
    }

    // ...other methods
}
```

2. Create a provider class that implements `ConnectionConfigurationRepositoryProvider`:

```java
package com.example;

import com.github.blausql.spi.connections.ConnectionConfigurationRepository;
import com.github.blausql.spi.connections.ConnectionConfigurationRepositoryProvider;

public class MyCustomRepositoryProvider implements ConnectionConfigurationRepositoryProvider {
    
    // You can use a singleton pattern here if your repository is stateless
    private static final ConnectionConfigurationRepository INSTANCE = new MyCustomRepository();
    
    @Override
    public ConnectionConfigurationRepository createRepository() {
        return INSTANCE;
    }
}
```

3. Register your provider using the Java ServiceLoader mechanism:

Create a file at `META-INF/services/com.github.blausql.spi.connections.ConnectionConfigurationRepositoryProvider` with the following content:

```
com.example.MyCustomRepositoryProvider
```

## Example: Database-backed Repository

Here's an example of a repository that stores connections in a database:

```java
package com.example;

import com.github.blausql.spi.connections.ConnectionConfigurationRepository;
import com.github.blausql.core.connection.ConnectionConfiguration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnectionRepository implements ConnectionConfigurationRepository {
    
    private final String dbUrl;
    
    public DatabaseConnectionRepository(String dbUrl) {
        this.dbUrl = dbUrl;
    }
    
    @Override
    public List<ConnectionConfiguration> getConnectionConfigurations() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM connections")) {
            
            List<ConnectionConfiguration> connections = new ArrayList<>();
            while (rs.next()) {
                ConnectionConfiguration connectionConfig = new ConnectionConfiguration(rs.getString("name"));
                connectionConfig.setDriverClassName(rs.getString("driver_class"));
                connectionConfig.setJdbcUrl(rs.getString("jdbc_url"));
                connectionConfig.setUserName(rs.getString("username"));
                // Set other properties...
                connections.add(connectionConfig);
            }
            return connections;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load connections", e);
        }
    }
    
    // Implement other methods...
}
```

## Notes

- Your implementation should be thread-safe as it may be accessed from multiple threads
- Make sure to properly handle exceptions and document any custom exceptions your implementation may throw
