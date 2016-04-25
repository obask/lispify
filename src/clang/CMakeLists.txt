cmake_minimum_required(VERSION 3.2)
project(untitled)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c11")

set(SOURCE_FILES main.c)
add_executable(untitled ${SOURCE_FILES} tokenizer.h parser.h)
