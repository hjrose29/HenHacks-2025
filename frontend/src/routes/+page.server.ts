import { StatusCodes } from "$lib/StatusCodes";
import { redirect, type ServerLoad } from "@sveltejs/kit";

export const load: ServerLoad = ({ locals }) => {
    if (!locals.user) {
        // redirect(StatusCodes.MOVED_TEMPORARILY, "/login")
    }

    return { user: locals.user }
}