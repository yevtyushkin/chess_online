class ServerUri {
  static const String _httpScheme = String.fromEnvironment(
    'httpScheme',
    defaultValue: 'http',
  );

  static const String _wsScheme = String.fromEnvironment(
    'wsScheme',
    defaultValue: 'ws',
  );

  static const String _host = String.fromEnvironment(
    'host',
    defaultValue: 'localhost',
  );

  static const String _port = String.fromEnvironment(
    'port',
    defaultValue: '8080',
  );

  static final Uri baseHttp = Uri(
    scheme: _httpScheme,
    host: _host,
    port: int.tryParse(_port),
  );

  static final Uri baseWs = Uri(
    scheme: _wsScheme,
    host: _host,
    port: int.tryParse(_port),
  );
}
