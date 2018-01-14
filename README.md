# Play Scala request-scoped DI example

This is an example of one way to create a "request scope" with Play and compile-time dependency injection (cake pattern). The same concept could be used in other contexts in Scala.

By a "scope", I mean a set of objects that are created once for the handling of a specific entity. For request scopes in Play, this is done by creating a new instance of the `HttpRequestHandler` to handle each request. The `HttpRequestHandler` and its dependencies are obtained from a separate `RequestComponents` cake, which can also add other components that depend on the `RequestHeader`.

There is a small bit of magic provided by a macro library called [`FastForward`](https://github.com/gmethvin/fastforward). The `forward` macro provided by this library takes a trait and implements its methods by forwarding to an object of your choice. In this case, all unimplemented methods in the request components are forwarded to the main application components, allowing the two to share components with relatively minimal boilerplate.

I explain other details of how things work in the comments of [AppLoader.scala](https://github.com/gmethvin/play-scala-request-scoped-di-example/blob/master/app/AppLoader.scala).
