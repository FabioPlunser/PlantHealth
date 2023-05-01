export function load (locals){
  const {user} = locals;
  if (!user) {
    return {status: 401};
  }
  return user;
};