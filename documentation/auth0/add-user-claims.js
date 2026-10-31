/**
 * Auth0 Post-Login Action adding user claims (copy of the one deployed in Auth0).
 *
 * Auth0 access tokens carry only the user id (sub), so this Action adds email and name to them. The backend reads
 * these claims to create the local user on the first login and to keep its username and email up to date
 * (CurrentUserProvider). The namespace must match CLAIMS_NAMESPACE there.
 *
 * Set up for each Auth0 tenant (test and production):
 *   1. Actions > Library > Create Action > Build from scratch, trigger "Login / Post Login".
 *   2. Paste this file, Deploy.
 *   3. Actions > Triggers > post-login: drag the Action into the flow, Apply.
 * Users get the claims from their next login (or the next token refresh).
 */
exports.onExecutePostLogin = async (event, api) => {
  const namespace = 'https://task.arturjarosz.com'; // must be a URL, can be anything you own

  if (event.user.email) {
    api.accessToken.setCustomClaim(`${namespace}/email`, event.user.email);
    api.accessToken.setCustomClaim(`${namespace}/email_verified`, event.user.email_verified);
  }
  if (event.user.name) {
    api.accessToken.setCustomClaim(`${namespace}/name`, event.user.name);
  }
};
