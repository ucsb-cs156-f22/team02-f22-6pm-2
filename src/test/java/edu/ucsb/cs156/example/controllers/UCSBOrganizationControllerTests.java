package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {
        
        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/UCSBOrganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/UCSBOrganization/all"))
                                .andExpect(status().is(200)); // logged
        }
        
        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/UCSBOrganization?orgCode=testCode1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }
        
        // Authorization tests for /api/UCSBOrganization/post
        // (Perhaps should also have these for put and delete)
        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/UCSBOrganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/UCSBOrganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganization() throws Exception {

                // arrange
                boolean testInactive1 = false;

                UCSBOrganization ucsbOrganization1 = UCSBOrganization.builder()
                                .orgCode("testCode")
                                .orgTranslation("testTranslation")
                                .orgTranslationShort("testTransShort")
                                .inactive(testInactive1)
                                .build();

                boolean testInactive2 = true;

                UCSBOrganization ucsbOrganization2 = UCSBOrganization.builder()
                                .orgCode("testCode")
                                .orgTranslation("testTranslation")
                                .orgTranslationShort("testTransShort")
                                .inactive(testInactive2)
                                .build();

                ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
                expectedOrganizations.addAll(Arrays.asList(ucsbOrganization1, ucsbOrganization2));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

                // act
                MvcResult response = mockMvc.perform(get("/api/UCSBOrganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // // Tests with mocks for database actions
        
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                boolean testInactive1 = true;

                UCSBOrganization ucsbOrganization = UCSBOrganization.builder()
                            .orgCode("testCode1")
                            .orgTranslation("testTranslation1")
                            .orgTranslationShort("testTransShort1")
                            .inactive(testInactive1)
                            .build();

                when(ucsbOrganizationRepository.findById(eq("testCode1"))).thenReturn(Optional.of(ucsbOrganization));

                // act
                MvcResult response = mockMvc.perform(get("/api/UCSBOrganization?orgCode=testCode1"))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById(eq("testCode1"));
                String expectedJson = mapper.writeValueAsString(ucsbOrganization);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationRepository.findById(eq("test"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/UCSBOrganization?orgCode=test"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("test"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id test not found", json.get("message"));
        }
        

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsborganization() throws Exception {
                // arrange

                boolean testInactive1 = true;

                UCSBOrganization ucsbOrganization1 = UCSBOrganization.builder()
                                .orgCode("testCode")
                                .orgTranslation("testTranslation")
                                .orgTranslationShort("testTransShort")
                                .inactive(testInactive1)
                                .build();

                when(ucsbOrganizationRepository.save(eq(ucsbOrganization1))).thenReturn(ucsbOrganization1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/UCSBOrganization/post?orgCode=testCode&orgTranslation=testTranslation&orgTranslationShort=testTransShort&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(ucsbOrganization1);
                String expectedJson = mapper.writeValueAsString(ucsbOrganization1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_an_organization() throws Exception {
                // arrange

                boolean testInactive1 = true;

                UCSBOrganization ucsbOrganization1 = UCSBOrganization.builder()
                                .orgCode("testCode")
                                .orgTranslation("testTranslation")
                                .orgTranslationShort("testTransShort")
                                .inactive(testInactive1)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("testCode"))).thenReturn(Optional.of(ucsbOrganization1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/UCSBOrganization?orgCode=testCode")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("testCode");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id testCode deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("testCode"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/UCSBOrganization?orgCode=testCode")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("testCode");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id testCode not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_commons() throws Exception {

                // arrange
                boolean testInactive1 = true;
                UCSBOrganization ucsbOrganizationOrig = UCSBOrganization.builder()
                                .orgCode("testCode1")
                                .orgTranslation("testTranslation1")
                                .orgTranslationShort("testTransShort1")
                                .inactive(testInactive1)
                                .build();

                boolean testInactive2 = false;
                UCSBOrganization ucsbOrganizationEdited = UCSBOrganization.builder()
                                .orgCode("testCode1")
                                .orgTranslation("testTranslation2")
                                .orgTranslationShort("testTransShort2")
                                .inactive(testInactive2)
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbOrganizationEdited);

                when(ucsbOrganizationRepository.findById(eq("testCode1"))).thenReturn(Optional.of(ucsbOrganizationOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/UCSBOrganization?orgCode=testCode1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("testCode1");
                verify(ucsbOrganizationRepository, times(1)).save(ucsbOrganizationEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
                // arrange

                boolean testInactive2 = false;
                UCSBOrganization ucsbOrganizationEdited = UCSBOrganization.builder()
                                .orgCode("testCode1")
                                .orgTranslation("testTranslation2")
                                .orgTranslationShort("testTransShort2")
                                .inactive(testInactive2)
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbOrganizationEdited);

                when(ucsbOrganizationRepository.findById(eq("testCode1"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/UCSBOrganization?orgCode=testCode1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("testCode1");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id testCode1 not found", json.get("message"));

        }
}
