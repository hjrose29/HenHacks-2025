import type { Handle } from "@sveltejs/kit";
import { env } from "$env/dynamic/public";

export const handle: Handle = async ({ event, resolve }) => {

    const sessionToken = event.cookies.get("salus_session");
    if (sessionToken) {
        const userReq = await fetch(`${env.PUBLIC_USER_SERVICE_URL}/api/users/me`);
        if (userReq.status == 200) {
            const user = await userReq.json();
            if (user) {
                event.locals.user = user;
            }
            event.cookies.delete("salus_session", { path: "/" });
        }
    }

    return await resolve(event);
}