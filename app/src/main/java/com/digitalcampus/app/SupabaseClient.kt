package com.digitalcampus.app

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://hrijejnjhugpavexyata.supabase.co",
    supabaseKey = "sb_publishable_BWwHgnqq7Wsx9yh3vgcUkA_On4A3g7i"
) {
    install(Auth)
    install(Postgrest)
}