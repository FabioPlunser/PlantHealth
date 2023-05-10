export async function load({ url, locals }) {
  if (locals.user) {
    return {
      permission: locals.user.permissions[0],
    };
  }

  return {
    permission: "GUEST",
  };
}
