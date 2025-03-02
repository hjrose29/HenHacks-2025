import type { Handle } from "@sveltejs/kit";
import { env } from "$env/dynamic/public";

export const handle: Handle = async ({ event, resolve }) => {

    const sessionToken = event.cookies.get("salus_session");
    if (sessionToken) {
        const userReq = await fetch(`${env.PUBLIC_USER_SERVICE_URL}/api/users/me`, { credentials: "include", headers: { "Cookie": `salus_session=${sessionToken}` } });
        console.log(userReq);
        if (userReq.status == 200) {
            const user = await userReq.json();
            if (user) {
                event.locals.user = user;
            }
        }
    }

    return await resolve(event);
}