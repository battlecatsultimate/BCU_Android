cmake_minimum_required(VERSION 3.4.1)

add_library(matrix-calc SHARED MatrixCalc.cpp)

find_library(log-lib log)

target_link_libraries(matrix-calc ${log-lib})