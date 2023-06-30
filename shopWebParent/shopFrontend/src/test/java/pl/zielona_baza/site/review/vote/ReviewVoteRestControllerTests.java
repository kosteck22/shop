package pl.zielona_baza.site.review.vote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.site.review.ReviewRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class ReviewVoteRestControllerTests {

    @Autowired private ReviewRepository reviewRepository;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    public void testVoteNotLogin() throws Exception {
        String requestURL = "/vote_review/1/up";

        MvcResult mvcResult = mockMvc.perform(post(requestURL).with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        VoteResult voteResult = objectMapper.readValue(json, VoteResult.class);

        assertFalse(voteResult.isSuccessful());
        assertThat(voteResult.getMessage()).contains("You must login");
    }

    @Test
    @WithMockUser(username = "tina.jamerson.2021@gmail.com", password = "tina2020")
    public void testVoteNonExistReview() throws Exception {
        String requestURL = "/vote_review/123/up";

        MvcResult mvcResult = mockMvc.perform(post(requestURL).with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        VoteResult voteResult = objectMapper.readValue(json, VoteResult.class);

        assertFalse(voteResult.isSuccessful());
        assertThat(voteResult.getMessage()).contains("no longer exists");
    }

    @Test
    @WithMockUser(username = "tina.jamerson.2021@gmail.com", password = "tina2020")
    public void testVoteUpReview() throws Exception {
        //given
        Integer reviewId = 10;
        String requestURL = "/vote_review/" + reviewId + "/up";

        Review review = reviewRepository.findById(reviewId).get();
        int voteCountBefore = review.getVotes();

        //when
        MvcResult mvcResult = mockMvc.perform(post(requestURL).with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        VoteResult voteResult = objectMapper.readValue(json, VoteResult.class);
        int voteCountAfter = voteResult.getVoteCount();

        //then
        assertTrue(voteResult.isSuccessful());
        assertThat(voteResult.getMessage()).contains("successfully voted up");
        assertEquals(voteCountAfter, voteCountBefore + 1);
    }

    @Test
    @WithMockUser(username = "tina.jamerson.2021@gmail.com", password = "tina2020")
    public void testUndoVoteUp() throws Exception {
        //given
        Integer reviewId = 10;
        String requestURL = "/vote_review/" + reviewId + "/up";

        Review review = reviewRepository.findById(reviewId).get();
        int voteCountBefore = review.getVotes();

        //when
        MvcResult mvcResult = mockMvc.perform(post(requestURL).with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        VoteResult voteResult = objectMapper.readValue(json, VoteResult.class);
        int voteCountAfter = voteResult.getVoteCount();

        //then
        assertTrue(voteResult.isSuccessful());
        assertThat(voteResult.getMessage()).contains("successfully voted up");
        assertEquals(voteCountAfter, voteCountBefore - 1);
    }
}
