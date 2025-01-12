package com.example.gmaps

// open function , ie kahi bh access ho skta hai , bina class ka function
//aur esme new syntax use kra function ka
    fun String.appendIfNotBlank(s: String): String = if (this != null && isNotBlank()) "$this$s" else ""