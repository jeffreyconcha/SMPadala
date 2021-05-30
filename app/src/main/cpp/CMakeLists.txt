cmake_minimum_required(VERSION 3.4.1)

add_library(
    smpadala
    SHARED
    smpadala.cpp
    )

find_library(
    smpadala
    log
    )

target_link_libraries(
    smpadala
    ${log-lib}
    )