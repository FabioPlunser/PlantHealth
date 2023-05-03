export async function load({ url, locals }) {
  // console.log("(app)/layout", locals);
  if (locals.user) {
    return {
      permission: locals.user.permissions[0],
    };
  }

  return {
    permission: "GUEST",
  };
}
