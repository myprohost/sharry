package sharry.backend.auth

import cats.effect._
import cats.implicits._

import sharry.backend.account._
import sharry.common._

/** Provides authentication from the configuration.
  *
  * This simply compares the password agains the fix string in the config, but only if the
  * username matches. Otherwise it let's the next login module try to authenticate.
  *
  * Note that this login module does ignore the AccountState on purpose. The config file
  * should not really be used, but serves as a fallback to easily access the application
  * in case other authentication is not possible.
  */
final class FixedAuth[F[_]: Async](cfg: AuthConfig, op: OAccount[F]) {

  private[this] val logger = sharry.logging.getLogger[F]

  def login: LoginModule[F] =
    LoginModule { up =>
      if (!cfg.fixed.enabled || !up.user.equalsIgnoreCase(cfg.fixed.user.id))
        (None: Option[LoginResult]).pure[F]
      else if (up.pass == cfg.fixed.password)
        for {
          _ <- logger.debug(s"Fixed auth: success for user ${cfg.fixed.user}")
          id <- addAccount(cfg.fixed)
          token <- AuthToken.user(id, cfg.serverSecret)
        } yield LoginResult.ok(token).some
      else
        logger.debug("Fixed auth: failed.") *>
          Option(LoginResult.invalidAuth).pure[F]
    }

  private def addAccount(cfg: AuthConfig.Fixed): F[AccountId] =
    AddAccount[F](cfg.user, true, AddAccount.AccountOps.from(op))

  def withPosition: (Int, LoginModule[F]) = (cfg.fixed.order, login)

}

object FixedAuth {
  def apply[F[_]: Async](cfg: AuthConfig, op: OAccount[F]): FixedAuth[F] =
    new FixedAuth[F](cfg, op)
}
