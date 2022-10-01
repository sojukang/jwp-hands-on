# JWP Hands-On

## 만들면서 배우는 스프링 실습 코드

### 학습 순서
- cache
- thread
- servlet
- reflection
- di

## ThreadPool
### PooledConnection interface
> An object that provides hooks for connection pool management. A PooledConnection object represents a physical connection to a data source. The connection can be recycled rather than being closed when an application is finished with it, thus reducing the number of connections that need to be made.
An application programmer does not use the PooledConnection interface directly; rather, it is used by a middle tier infrastructure that manages the pooling of connections.
When an application calls the method DataSource.getConnection, it gets back a Connection object. If connection pooling is being done, that Connection object is actually a handle to a PooledConnection object, which is a physical connection.
The connection pool manager, typically the application server, maintains a pool of PooledConnection objects. If there is a PooledConnection object available in the pool, the connection pool manager returns a Connection object that is a handle to that physical connection. If no PooledConnection object is available, the connection pool manager calls the ConnectionPoolDataSource method getPoolConnection to create a new physical connection. The JDBC driver implementing ConnectionPoolDataSource creates a new PooledConnection object and returns a handle to it.
When an application closes a connection, it calls the Connection method close. When connection pooling is being done, the connection pool manager is notified because it has registered itself as a ConnectionEventListener object using the ConnectionPool method addConnectionEventListener. The connection pool manager deactivates the handle to the PooledConnection object and returns the PooledConnection object to the pool of connections so that it can be used again. Thus, when an application closes its connection, the underlying physical connection is recycled rather than being closed.
If the connection pool manager wraps or provides a proxy to the logical handle returned from a call to PoolConnection.getConnection, the pool manager must do one of the following when the connection pool manager closes or returns the PooledConnection to the pool in response to the application calling Connection.close:
call endRequest on the logical Connection handle
call close on the logical Connection handle
The physical connection is not closed until the connection pool manager calls the PooledConnection method close. This method is generally called to have an orderly shutdown of the server or if a fatal error has made the connection unusable.
A connection pool manager is often also a statement pool manager, maintaining a pool of PreparedStatement objects. When an application closes a prepared statement, it calls the PreparedStatement method close. When Statement pooling is being done, the pool manager is notified because it has registered itself as a StatementEventListener object using the ConnectionPool method addStatementEventListener. Thus, when an application closes its PreparedStatement, the underlying prepared statement is recycled rather than being closed.
> --- 
> Connection Pool 관리를 위한 후크를 제공하는 객체입니다. PooledConnection 객체는 DataSource에 대한 물리적 연결을 나타냅니다. 연결은 응용 프로그램이 완료될 때 닫히지 않고 재활용될 수 있으므로 만들어야 하는 연결 수를 줄일 수 있습니다.
애플리케이션 프로그래머는 PooledConnection 인터페이스를 직접 사용하지 않습니다. 오히려 Connection Pooling을 관리하는 중간 계층 인프라에서 사용됩니다.
>> 응용 프로그램이 DataSource.getConnection 메서드를 호출하면 Connection 객체를 다시 가져옵니다. Connection Pooling이 수행되는 경우 해당 Connection 객체는 실제로 물리적 연결인 PooledConnection 객체를 다룰 수 있습니다.
Connection Pool 관리자(일반적으로 응용 프로그램 서버)는 PooledConnection 객체 풀을 유지 관리합니다. 풀에서 사용 가능한 PooledConnection 객체가 있는 경우 Connection Pool 관리자는 해당 물리적 ​​연결에 대한 핸들인 Connection 객체를 반환합니다.
PooledConnection 객체를 사용할 수 없는 경우 Connection Pool 관리자는 ConnectionPoolDataSource 메서드 getPoolConnection 을 호출하여 새 물리적 연결을 만듭니다. ConnectionPoolDataSource 를 구현하는 JDBC 드라이버는 새로운 PooledConnection 객체를 생성하고 이에 대한 핸들을 반환합니다.
응용 프로그램이 연결을 닫으면 Connection 메서드 close 를 호출합니다. Connection Pooling이 수행될 때 Connection Pool 관리자는 ConnectionPool 메소드 addConnectionEventListener 를 사용하여 ConnectionEventListener 객체로 자신을 등록했기 때문에 알림을 받습니다. Connection Pool 관리자는 PooledConnection 객체에 대한 핸들을 비활성화하고 PooledConnection 객체를 Connection Pool로 반환하여 다시 사용할 수 있도록 합니다. 따라서 응용 프로그램이 연결을 닫으면 기본 물리적 연결이 닫히지 않고 재활용됩니다.
> 
> Connection Pool 관리자가 PoolConnection.getConnection 호출에서 반환된 논리적 핸들에 프록시를 래핑하거나 제공하는 경우 Connection Pool 관리자가 애플리케이션에 대한 응답으로 PooledConnection 을 닫거나 풀에 반환할 때 풀 관리자는 다음 중 하나를 수행해야 합니다. Connection.close 호출 :
논리적 Connection 핸들에서 endRequest 호출
논리적 Connection 핸들에서 close 호출
Connection Pool 관리자가 PooledConnection 메소드 close 를 호출할 때까지 물리적 연결은 닫히지 않습니다. 이 메서드는 일반적으로 서버를 순서대로 종료하거나 치명적인 오류로 인해 연결을 사용할 수 없는 경우 호출됩니다.
Connection Pool 관리자는 종종 PreparedStatement 객체 풀을 유지 관리하는 명령문 풀 관리자이기도 합니다. 응용 프로그램이 Prepared 문을 닫을 때 PreparedStatement 메서드 close 를 호출합니다. Statement 풀링이 완료되면 ConnectionPool 메소드 addStatementEventListener 를 사용하여 자신을 StatementEventListener 객체로 등록했기 때문에 풀 관리자에게 알려줍니다. 따라서 응용 프로그램이 PreparedStatement 를 닫을 때 기본 Prepared 문은 닫히지 않고 재활용됩니다.

## JdbcConnectionPool(H2)
### ThreadPool에서 getConnection을 호출
getConnection -> getConnectionNow -> 

#### Pool에 Connection 존재하지 않을 경우 
Datasource에서 getPooledConnection -> getXAConnection
-> return new JdbcXAConnection

#### Pool에 Connection 존재할 경우

### Connection이 close될 경우
addEventListener에 의해 등록된 ConnectionEventListener가 close나 error 이벤트가 발생하면
(connectionClosed(ConnectionEvent event) or connectionErrorOccurred(ConnectionEvent event) 호출)
EventListener를 제거하고 recycleConnection(PooledConnection)을 호출하여 Connection을 Pool로 되돌린다. 

### getActiveConnections
getConnection에 의해 호출되고 활성화되어(열린; open) 아직 close 되지 않은 Connection의 개수를 반환한다.


## XA Transaction
Database 2개 이상을 사용하여 Transaction을 수행할 때를 위해 정의된 표준
2PC(2 phase commit)을 통한 분산 트랜잭션 처리를 위한 X-Open에서 명시한 표준
Database 2개 이상을 사용해도 트랜잭션을 보장할 필요가 없을 경우 Non-XA-datasource 사용

#### 2PC
begin -> end -> prepare -> commit

#### 1PC
begin -> end -> commit

## HicariCP

### MySQL Configuration
In order to get the best performance out of MySQL, these are some of our recommended settings. There are many other performance related settings available in MySQL and we recommend reviewing them all to ensure you are getting the best performance for your application.

`prepStmtCacheSize`
This sets the number of prepared statements that the MySQL driver will cache per connection. The default is a conservative 25. We recommend setting this to between 250-500.

`prepStmtCacheSqlLimit`
This is the maximum length of a prepared SQL statement that the driver will cache. The MySQL default is 256. In our experience, especially with ORM frameworks like Hibernate, this default is well below the threshold of generated statement lengths. Our recommended setting is 2048.

`cachePrepStmts`
Neither of the above parameters have any effect if the cache is in fact disabled, as it is by default. You must set this parameter to true.

`useServerPrepStmts` : Newer versions of MySQL support server-side prepared statements, this can provide a substantial performance boost. Set this property to true.

--- 
#### 주요 부분 번역
MySQL에서 최상의 성능을 얻기 위해 권장하는 설정 중 일부입니다. 

`prepStmtCacheSize`
MySQL 드라이버가 연결당 캐시할 Prepared Statements의 수를 설정합니다. 기본값은 보수적인 25 입니다. 250-500 사이로 설정하는 것이 좋습니다 .

`prepStmtCacheSqlLimit`
드라이버가 캐시할 Prepared SQL 문의 최대 길이입니다. MySQL 기본값은 256 입니다. 우리의 경험, 특히 Hibernate와 같은 ORM 프레임워크에서 이 기본값은 생성된 명령문 길이의 임계값보다 훨씬 낮습니다. 권장 설정은 2048 입니다.

`cachePrepStmts`
캐시가 기본적으로 비활성화되어 있는 경우 위의 매개변수 중 어느 것도 영향을 미치지 않습니다. 이 매개변수를 true로 설정해야 합니다.

`useServerPrepStmts`: 최신 버전의 MySQL은 서버 측 Prepared Statements를 지원하므로 상당한 성능 향상을 제공할 수 있습니다. 이 속성을 true로 설정하십시오.

