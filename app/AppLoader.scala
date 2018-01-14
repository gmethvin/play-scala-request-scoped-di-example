
import io.methvin.fastforward._
import play.api.http._
import play.api.mvc._
import play.api.routing.Router
import play.api.{controllers => _, _}
import play.core.SourceMapper
import play.filters.HttpFiltersComponents

class AppLoader extends ApplicationLoader {
  def load(context: ApplicationLoader.Context): Application = new AppComponents(context).application
}

// Application-scoped components
class AppComponents(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
    // Note: httpFilters would need to be defined in RequestComponents if a filter depends on the request.
    with HttpFiltersComponents
    with controllers.AssetsComponents {

  // This is the global HttpRequestHandler. This obtains an instance of the request-scoped HttpRequestHandler from
  // RequestComponents to handle each request. To do this, it implements the request method of RequestComponents,
  // then asks forward to implement the rest of the methods by forwarding the method calls to this.
  override lazy val httpRequestHandler: HttpRequestHandler = (rh: RequestHeader) => {
    trait RequestComponentsWithRequest extends RequestComponents {
      val requestHeader: RequestHeader = rh
    }
    val requestComponents = forward[RequestComponentsWithRequest](this)
    requestComponents.httpRequestHandler.handlerForRequest(rh)
  }

  // This HttpErrorHandler is used by the server in case the HttpRequestHandler throws an exception. It will also be
  // used by components like filters defined in the application scope. It should be okay to not pass a router here,
  // since errors that happen in filters or at the server level generally are unrelated to routing, and the router is
  // only used in dev mode to show the routes tried after 404 errors.
  override lazy val httpErrorHandler: HttpErrorHandler =
    new DefaultHttpErrorHandler(environment, configuration, sourceMapper, router = None)

  // Note: the router is defined in the request scope since we want our controllers to be request-scoped.
  // This router now is no longer used, so we throw an exception if it is.
  override lazy val router: Router = movedToRequestScope

  // A helper to throw an exception if we move something from BuiltInComponents to RequestComponents. We could avoid
  // having to do this if we decided not to use the BuiltInComponents from Play and created our own trait.
  @inline private def movedToRequestScope: Nothing = throw new UnsupportedOperationException(
    "This method is not meant to be used. Use the method on RequestComponents."
  )
}

// Request-scoped components
trait RequestComponents {
  def requestHeader: RequestHeader

  lazy val homeController =
    new controllers.HomeController(requestHeader, controllerComponents)
  lazy val router: Router =
    new _root_.router.Routes(httpErrorHandler, homeController, assets)
  lazy val httpErrorHandler: HttpErrorHandler =
    new DefaultHttpErrorHandler(environment, configuration, sourceMapper, Some(router))
  lazy val httpRequestHandler: HttpRequestHandler = {
    println(requestHeader)
    new DefaultHttpRequestHandler(router, httpErrorHandler, httpConfiguration, httpFilters: _*)
  }

  // The below methods are automatically forwarded to the main components by the forward macro.
  //
  // You could avoid this if you make RequestComponents an inner trait, since you could reference the member from the
  // outer class, but you'll still likely need to forward methods if you mix in other traits that express dependencies
  // as abstract methods. Also MacWire (and macros in general) won't allow you to satisfy dependencies using members
  // of an outer scope.

  def environment: Environment
  def configuration: Configuration
  def controllerComponents: ControllerComponents
  def sourceMapper: Option[SourceMapper]
  def httpConfiguration: HttpConfiguration
  def httpFilters: Seq[EssentialFilter]
  def assets: controllers.Assets
}
