package com.example.demo.controller;

import com.example.demo.entity.Bookshelf;
import com.example.demo.entity.Story;
import com.example.demo.entity.User;
import com.example.demo.repository.BookshelfRepository;
import com.example.demo.repository.ReadingProgressRepository;
import com.example.demo.repository.StoryRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // THÊM DÒNG NÀY
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookshelfController.class)
@AutoConfigureMockMvc(addFilters = true)// Chỉ load Controller này để test đơn lẻ
public class BookshelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookshelfRepository bookshelfRepository;
    @MockBean
    private ReadingProgressRepository progressRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private StoryRepository storyRepository;

    private User mockUser;
    private Story mockStory;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).username("testuser").build();
        mockStory = Story.builder().id(10L).title("Truyện Test").slug("truyen-test").build();
    }

    // --- TEST HÀM SHOW BOOKSHELF (GET) ---

    @Test
    @WithMockUser(username = "testuser") // Giả lập User đã đăng nhập
    public void testShowBookshelf_Success() throws Exception {
        // Giả lập dữ liệu trả về từ Repository
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(bookshelfRepository.findByUserIdOrderByAddedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(progressRepository.findByUserIdOrderByLastReadAtDesc(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/bookshelf"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/bookshelf")) // Kiểm tra trả về đúng file HTML
                .andExpect(model().attributeExists("bookshelf"))
                .andExpect(model().attributeExists("progressList"));
    }

    // --- TEST HÀM ADD TO BOOKSHELF (POST) ---

    @Test
    @WithMockUser(username = "testuser")
    public void testAddToBookshelf_NewStory_ShouldSave() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(storyRepository.findById(10L)).thenReturn(Optional.of(mockStory));
        // Giả lập truyện CHƯA có trong kệ
        when(bookshelfRepository.findByUserIdAndStoryId(1L, 10L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/bookshelf/add")
                        .param("storyId", "10")
                        .with(csrf())) // Spring Security yêu cầu CSRF token cho POST
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/story/truyen-test")); // Kiểm tra Redirect đúng Slug

        verify(bookshelfRepository, times(1)).save(any(Bookshelf.class)); // Xác nhận hàm save đã được gọi
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testAddToBookshelf_ExistingStory_ShouldNotSave() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(storyRepository.findById(10L)).thenReturn(Optional.of(mockStory));
        // Giả lập truyện ĐÃ CÓ trong kệ
        when(bookshelfRepository.findByUserIdAndStoryId(1L, 10L)).thenReturn(Optional.of(new Bookshelf()));

        mockMvc.perform(post("/bookshelf/add")
                        .param("storyId", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/story/truyen-test"));

        verify(bookshelfRepository, never()).save(any(Bookshelf.class)); // Xác nhận KHÔNG gọi hàm save để tránh trùng lặp
    }

    // --- TEST BẢO MẬT (SECURITY) ---

    @Test
    public void testShowBookshelf_Unauthenticated_ShouldReturn401() throws Exception {
    mockMvc.perform(get("/user/bookshelf"))
            .andExpect(status().isUnauthorized()); // Thay vì is3xxRedirection()
    }
}