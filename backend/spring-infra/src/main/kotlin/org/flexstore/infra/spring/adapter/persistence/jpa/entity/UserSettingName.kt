package org.flexstore.infra.spring.adapter.persistence.jpa.entity

// The authorized names of the settings of a customer, as stored (the ones of the check of user_setting.name)
enum class UserSettingName(val stored: String) {
    COLOR_MODE("color_mode"),
    LANGUAGE("language"),
    GUIDED_TOUR("guided_tour"),
    COOKIE_CHOICE("cookie_choice")
}
