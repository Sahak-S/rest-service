package com.example.restservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class TasksRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ImMemTaskRepository taskRepository;

    @AfterEach
    void tearDown(){
        this.taskRepository.getTasks().clear();
    }

    @Test
    void handleGetAllTasks_ReturnsValidResponseEntity() throws Exception {
        // given
        var requestBuilder = get("/api/tasks");
        this.taskRepository.getTasks()
                .addAll(List.of(new Task(UUID.fromString("71117396-8694-11ed-9ef6-77042ee83937"), "arajin ashxatanq", false),
                        new Task(UUID.fromString("7172d834-8694-11ed-8669-d7b17d45fba8"), "erkrord ashxatanq", true)));

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                    {
                                        "id": "71117396-8694-11ed-9ef6-77042ee83937",
                                        "details": "arajin ashxatanq",
                                        "completed": false
                                    },
                                    {
                                        "id": "7172d834-8694-11ed-8669-d7b17d45fba8",
                                        "details": "erkrord ashxatanq",
                                        "completed": true
                                    }
                                ]
                                """)
                );

    }

    @Test
    void handleCreateNewTask_PayloadIsValid_ReturnsValidResponseEntity() throws Exception {
        // given
        var requestBuilder = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "details": "errord ashxatanq"
                        }
                        """);

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "details": "errord ashxatanq",
                                    "completed": false
                                }
                                """),
                        jsonPath("$.id").exists()
                );
        assertEquals(1,this.taskRepository.getTasks().size());
        final var task = this.taskRepository.getTasks().get(0);
        assertNotNull(task.id());
        assertEquals("errord ashxatanq",task.details());
        assertFalse(task.completed());
    }


    @Test
    void handleCreateNewTask_PayloadIsInvalid_ReturnsValidResponseEntity() throws Exception {
        // given
        var requestBuilder = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                .content("""
                        {
                            "details": null
                        }
                        """);

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "errors": ["Task details mast be set"]
                                }
                                """, true)
                );

        assertEquals(1,this.taskRepository.getTasks().size());
        final var task = this.taskRepository.getTasks().get(0);
        assertNotNull(task.id());
        assertEquals("errord ashxatanq",task.details());
        assertFalse(task.completed());
    }

}