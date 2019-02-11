package com.xinxin.jhi.web.rest;

import static com.xinxin.jhi.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.describedAs;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.xinxin.jhi.JhipsterSingleReactApp;
import com.xinxin.jhi.domain.Author;
import com.xinxin.jhi.domain.Book;
import com.xinxin.jhi.repository.AuthorRepository;
import com.xinxin.jhi.repository.BookRepository;
import com.xinxin.jhi.service.AuthorService;
import com.xinxin.jhi.service.dto.AuthorDTO;
import com.xinxin.jhi.service.mapper.AuthorMapper;
import com.xinxin.jhi.service.util.RandomUtil;
import com.xinxin.jhi.web.rest.errors.ExceptionTranslator;

/**
 * Test class for the AuthorResource REST controller.
 *
 * @see AuthorResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JhipsterSingleReactApp.class)
public class AuthorResourceIntTest {

	private static final String DEFAULT_NAME = "AAAAAAAAAA";
	private static final String UPDATED_NAME = "BBBBBBBBBB";

	private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.ofEpochDay(0L);
	private static final LocalDate UPDATED_BIRTH_DATE = LocalDate.now(ZoneId.systemDefault());

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private AuthorMapper authorMapper;

	@Autowired
	private AuthorService authorService;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private MappingJackson2HttpMessageConverter jacksonMessageConverter;

	@Autowired
	private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

	@Autowired
	private ExceptionTranslator exceptionTranslator;

	@Autowired
	private EntityManager em;

	private MockMvc restAuthorMockMvc;

	private Author author;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		final AuthorResource authorResource = new AuthorResource(authorService);
		this.restAuthorMockMvc = MockMvcBuilders.standaloneSetup(authorResource).setCustomArgumentResolvers(pageableArgumentResolver).setControllerAdvice(exceptionTranslator)
				.setConversionService(createFormattingConversionService()).setMessageConverters(jacksonMessageConverter).build();
	}

	/**
	 * Create an entity for this test.
	 *
	 * This is a static method, as tests for other entities might also need it, if
	 * they test an entity which requires the current entity.
	 */
	public static Author createEntity(EntityManager em) {
		Author author = new Author().name(DEFAULT_NAME).birthDate(DEFAULT_BIRTH_DATE);
		return author;
	}

	@Before
	public void initTest() {
		author = createEntity(em);
	}

	@Test
	public void batchCreate() throws IOException {
		String pathString = "C:\\Users\\lenovo\\Downloads\\_download_after_2018_11_11\\宋词三百首全集_古诗文网.html";
		List<String> readLines = FileUtils.readLines(new File(pathString));

		Map<String, Author> map = new HashMap<>();
		int i = 0;
		for (String string : readLines) {
			i++;
			if (string.indexOf("<a") != -1) {
				List<String> collect = Arrays.stream(string.split("target=\"_blank\">")).filter(s -> s.contains("</a>")).collect(Collectors.toList());
				for (String string2 : collect) {
					String[] split = string2.split("</a>");
					String book = split[0];
					String name = "";
					if (split[1].contains("(")) {
						name = split[1].substring(split[1].lastIndexOf("(") + 1, split[1].lastIndexOf(")"));
					} else {
						book = split[0].substring(0, split[1].lastIndexOf("("));
						name = split[0].substring(split[1].lastIndexOf("(") + 1, split[1].lastIndexOf(")"));
					}
					System.out.println(book + "\t" + name);

					Author dest = map.get(name);

					if (dest == null) {
						Author author = new Author();
						author.setName(name);
						author.setBirthDate(getDate());
						Author save = authorRepository.save(author);
						map.put(name, save);
						dest = save;
					}

					Book entity = new Book();
					entity.setDescription("desc" + i);
					entity.setPrice(new BigDecimal(i));
					entity.setPublicationDate(getDate());
					entity.setTitle("title" + i);
					entity.setAuthor(dest);
					bookRepository.save(entity);

				}
			}
		}
	}

	public LocalDate getDate() {
		String dateString = RandomUtils.nextInt(1, 2000) + "-" + RandomUtils.nextInt(1, 12) + "-" + RandomUtils.nextInt(1, 30);
		Date parseDate = new Date();
		try {
			parseDate = DateUtils.parseDate(dateString, "yyyy-MM-dd");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	@Test
	public void test() throws ParseException {
		for (int i = 0; i < 200; i++) {
			getDate();
		}
	}

	@Test
	@Transactional
	public void createAuthor() throws Exception {
		int databaseSizeBeforeCreate = authorRepository.findAll().size();

		// Create the Author
		AuthorDTO authorDTO = authorMapper.toDto(author);
		restAuthorMockMvc.perform(post("/api/authors").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(authorDTO))).andExpect(status().isCreated());

		// Validate the Author in the database
		List<Author> authorList = authorRepository.findAll();
		assertThat(authorList).hasSize(databaseSizeBeforeCreate + 1);
		Author testAuthor = authorList.get(authorList.size() - 1);
		assertThat(testAuthor.getName()).isEqualTo(DEFAULT_NAME);
		assertThat(testAuthor.getBirthDate()).isEqualTo(DEFAULT_BIRTH_DATE);
	}

	@Test
	@Transactional
	public void createAuthorWithExistingId() throws Exception {
		int databaseSizeBeforeCreate = authorRepository.findAll().size();

		// Create the Author with an existing ID
		author.setId(1L);
		AuthorDTO authorDTO = authorMapper.toDto(author);

		// An entity with an existing ID cannot be created, so this API call must fail
		restAuthorMockMvc.perform(post("/api/authors").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(authorDTO))).andExpect(status().isBadRequest());

		// Validate the Author in the database
		List<Author> authorList = authorRepository.findAll();
		assertThat(authorList).hasSize(databaseSizeBeforeCreate);
	}

	@Test
	@Transactional
	public void checkNameIsRequired() throws Exception {
		int databaseSizeBeforeTest = authorRepository.findAll().size();
		// set the field null
		author.setName(null);

		// Create the Author, which fails.
		AuthorDTO authorDTO = authorMapper.toDto(author);

		restAuthorMockMvc.perform(post("/api/authors").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(authorDTO))).andExpect(status().isBadRequest());

		List<Author> authorList = authorRepository.findAll();
		assertThat(authorList).hasSize(databaseSizeBeforeTest);
	}

	@Test
	@Transactional
	public void getAllAuthors() throws Exception {
		// Initialize the database
		authorRepository.saveAndFlush(author);

		// Get all the authorList
		restAuthorMockMvc.perform(get("/api/authors?sort=id,desc")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.[*].id").value(hasItem(author.getId().intValue()))).andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
				.andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString())));
	}

	@Test
	@Transactional
	public void getAuthor() throws Exception {
		// Initialize the database
		authorRepository.saveAndFlush(author);

		// Get the author
		restAuthorMockMvc.perform(get("/api/authors/{id}", author.getId())).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(jsonPath("$.id").value(author.getId().intValue())).andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
				.andExpect(jsonPath("$.birthDate").value(DEFAULT_BIRTH_DATE.toString()));
	}

	@Test
	@Transactional
	public void getNonExistingAuthor() throws Exception {
		// Get the author
		restAuthorMockMvc.perform(get("/api/authors/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
	}

	@Test
	@Transactional
	public void updateAuthor() throws Exception {
		// Initialize the database
		authorRepository.saveAndFlush(author);

		int databaseSizeBeforeUpdate = authorRepository.findAll().size();

		// Update the author
		Author updatedAuthor = authorRepository.findById(author.getId()).get();
		// Disconnect from session so that the updates on updatedAuthor are not directly
		// saved in db
		em.detach(updatedAuthor);
		updatedAuthor.name(UPDATED_NAME).birthDate(UPDATED_BIRTH_DATE);
		AuthorDTO authorDTO = authorMapper.toDto(updatedAuthor);

		restAuthorMockMvc.perform(put("/api/authors").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(authorDTO))).andExpect(status().isOk());

		// Validate the Author in the database
		List<Author> authorList = authorRepository.findAll();
		assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
		Author testAuthor = authorList.get(authorList.size() - 1);
		assertThat(testAuthor.getName()).isEqualTo(UPDATED_NAME);
		assertThat(testAuthor.getBirthDate()).isEqualTo(UPDATED_BIRTH_DATE);
	}

	@Test
	@Transactional
	public void updateNonExistingAuthor() throws Exception {
		int databaseSizeBeforeUpdate = authorRepository.findAll().size();

		// Create the Author
		AuthorDTO authorDTO = authorMapper.toDto(author);

		// If the entity doesn't have an ID, it will throw BadRequestAlertException
		restAuthorMockMvc.perform(put("/api/authors").contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(authorDTO))).andExpect(status().isBadRequest());

		// Validate the Author in the database
		List<Author> authorList = authorRepository.findAll();
		assertThat(authorList).hasSize(databaseSizeBeforeUpdate);
	}

	@Test
	@Transactional
	public void deleteAuthor() throws Exception {
		// Initialize the database
		authorRepository.saveAndFlush(author);

		int databaseSizeBeforeDelete = authorRepository.findAll().size();

		// Get the author
		restAuthorMockMvc.perform(delete("/api/authors/{id}", author.getId()).accept(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().isOk());

		// Validate the database is empty
		List<Author> authorList = authorRepository.findAll();
		assertThat(authorList).hasSize(databaseSizeBeforeDelete - 1);
	}

	@Test
	@Transactional
	public void equalsVerifier() throws Exception {
		TestUtil.equalsVerifier(Author.class);
		Author author1 = new Author();
		author1.setId(1L);
		Author author2 = new Author();
		author2.setId(author1.getId());
		assertThat(author1).isEqualTo(author2);
		author2.setId(2L);
		assertThat(author1).isNotEqualTo(author2);
		author1.setId(null);
		assertThat(author1).isNotEqualTo(author2);
	}

	@Test
	@Transactional
	public void dtoEqualsVerifier() throws Exception {
		TestUtil.equalsVerifier(AuthorDTO.class);
		AuthorDTO authorDTO1 = new AuthorDTO();
		authorDTO1.setId(1L);
		AuthorDTO authorDTO2 = new AuthorDTO();
		assertThat(authorDTO1).isNotEqualTo(authorDTO2);
		authorDTO2.setId(authorDTO1.getId());
		assertThat(authorDTO1).isEqualTo(authorDTO2);
		authorDTO2.setId(2L);
		assertThat(authorDTO1).isNotEqualTo(authorDTO2);
		authorDTO1.setId(null);
		assertThat(authorDTO1).isNotEqualTo(authorDTO2);
	}

	@Test
	@Transactional
	public void testEntityFromId() {
		assertThat(authorMapper.fromId(42L).getId()).isEqualTo(42);
		assertThat(authorMapper.fromId(null)).isNull();
	}
}
