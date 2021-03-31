package shop.http.routes

import shop.domain.brand._
import shop.generators._
import shop.services.Brands

import cats.effect._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.scalacheck.Gen
import suite.HttpSuite

object BrandRoutesSuite extends HttpSuite {

  def dataBrands(brands: List[Brand]) = new TestBrands {
    override def findAll: IO[List[Brand]] =
      IO.pure(brands)
  }

  def failingBrands(brands: List[Brand]) = new TestBrands {
    override def findAll: IO[List[Brand]] =
      IO.raiseError(DummyError) *> IO.pure(brands)
  }

  test("GET brands succeeds") {
    forall(Gen.listOf(brandGen)) { b =>
      val req    = GET(Uri.uri("/brands"))
      val routes = new BrandRoutes[IO](dataBrands(b)).routes
      assertHttp(routes, req)(Status.Ok, b)
    }
  }

  test("GET brands fails") {
    forall(Gen.listOf(brandGen)) { b =>
      val req    = GET(Uri.uri("/brands"))
      val routes = new BrandRoutes[IO](failingBrands(b)).routes
      assertHttpFailure(routes, req)
    }
  }

}

protected class TestBrands extends Brands[IO] {
  def create(name: BrandName): IO[Unit] = IO.unit
  def findAll: IO[List[Brand]]          = IO.pure(List.empty)
}
