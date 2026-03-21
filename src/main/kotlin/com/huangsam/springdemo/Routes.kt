package com.huangsam.springdemo

/**
 * Centralized route constants for cross-package usage and testing.
 *
 * Add routes here only if:
 * - They are referenced in multiple controllers/packages (reduces coupling)
 * - They are used in tests (prevents test fragility from hardcoded strings)
 *
 * Local routes that live in a single controller (e.g., /admin) should remain hardcoded in
 * their @RequestMapping annotations.
 */
public object Routes {
    public const val API_ARTICLE: String = "/api/article"
    public const val API_USER: String = "/api/user"
    public const val API_COMMENT: String = "/api/comment"
    public const val API_CATEGORY: String = "/api/category"
    public const val API_TAG: String = "/api/tag"
    public const val ARTICLE: String = "/article"
    public const val CATEGORY: String = "/category"
    public const val TAG: String = "/tag"
    public const val USER: String = "/user"
    public const val ROOT: String = "/"
}
