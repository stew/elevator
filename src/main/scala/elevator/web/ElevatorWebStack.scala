package elevator.web

import org.scalatra.NotFound
import org.scalatra._
import scalate.ScalateSupport
import org.fusesource.scalate.{ TemplateEngine, Binding }
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import javax.servlet.http.HttpServletRequest
import collection.mutable
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s._
import JsonDSL._

trait ElevatorWebStack
    extends ScalatraServlet
    with ScalateSupport
    with JValueResult
    with JacksonJsonSupport
    with SessionSupport
    with AtmosphereSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats
}
