import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.flywaydb.core.Flyway;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Updated by balajimore on 2019-06-01.
 */
class TestsWithFullTypeSafe {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("NewPersistenceUnit");
    private static final EntityManager entityManager = emf.createEntityManager();

    @BeforeAll
    static void prepareForTests() {
        prepareDatabaseForTests();
    }

    @AfterAll
    static void closeEntityManager() {
        entityManager.close();
    }

    private static void prepareDatabaseForTests() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:file:./database", null, null);
        flyway.migrate();
    }

    @Test
    void getAllBooksOrderByTitle() {
        TypedQuery<Book> jpql_query =
                entityManager.createQuery("" +
                                "SELECT b " +
                                "FROM Book b " +
                                "ORDER BY b.title",
                        Book.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cc_query = cb.createQuery(Book.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cc_query_root)
                .orderBy(cb.asc(cc_query_root.get("title")));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .isEqualTo(jpql_query.getResultList());
    }

    @Test
    void getAllAnnouncementsOrderByTitle() {
        TypedQuery<Announcement> jpql_query =
                entityManager.createQuery("" +
                                "SELECT a " +
                                "FROM Announcement a " +
                                "ORDER BY a.a_id",
                        Announcement.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Announcement> cc_query = cb.createQuery(Announcement.class);
        Root<Announcement> cc_query_root = cc_query.from(Announcement.class);

        cc_query.select(cc_query_root)
                .orderBy(cb.asc(cc_query_root.get("a_id")));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .isEqualTo(jpql_query.getResultList());
    }

    @Test
    void getBooksByTitleLike() {
        String titleLike = "Lord%";

        TypedQuery<Book> jpql_query = entityManager.createQuery("" +
                        "SELECT b " +
                        "FROM Book b " +
                        "WHERE b.title LIKE :like",
                Book.class)
                .setParameter("like", titleLike);


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cc_query = cb.createQuery(Book.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cc_query_root)
                .where(cb.like(cc_query_root.get(Book_.title), titleLike));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    /**
     * the easiest way:
     * "SELECT DISTINCT b.bookstore " +
     * "FROM Book b " +
     * "WHERE b.title LIKE :title"
     * <p>
     * example below shows how to use subqueries in 'IN' clause
     */
    @Test
    void getBookstoresWithTitlesLike() {
        String titleLike = "Lord%";

        TypedQuery<Bookstore> jpql_query = entityManager.createQuery("" +
                        "SELECT bookstore " +
                        "FROM Bookstore bookstore JOIN bookstore.books books " +
                        "WHERE books IN (" +
                        "SELECT book " +
                        "FROM Book book " +
                        "WHERE book.title LIKE :title)",
                Bookstore.class)
                .setParameter("title", titleLike);


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bookstore> cc_query = cb.createQuery(Bookstore.class);
        Root<Bookstore> cc_query_root = cc_query.from(Bookstore.class);
        Join<Bookstore, Book> books = cc_query_root.join(Bookstore_.books);

        Subquery<Book> cc_subquery = cc_query.subquery(Book.class);
        Root<Book> cc_subquery_root = cc_subquery.from(Book.class);
        cc_subquery.select(cc_subquery_root)
                .where(cb.like(cc_subquery_root.get(Book_.title), titleLike));

        cc_query.select(cc_query_root)
                .where(cb.in(books).value(cc_subquery));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getBooksWithPriceIn() {
        ImmutableSet<Integer> prices = ImmutableSet.of(10, 15, 20);

        TypedQuery<Book> jpql_query =
                entityManager.createQuery("" +
                                "SELECT b " +
                                "FROM Book b " +
                                "WHERE b.price IN :prices",
                        Book.class)
                        .setParameter("prices", prices);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cc_query = cb.createQuery(Book.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cc_query_root)
                .where(cc_query_root.get(Book_.price).in(prices));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getAllBookTitles() {
        TypedQuery<String> jpql_query = entityManager.createQuery("" +
                        "SELECT b.title " +
                        "FROM Book b",
                String.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> cc_query = cb.createQuery(String.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);
        cc_query.select(cc_query_root.get(Book_.title));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getBooksWithPriceMoreThan() {
        int value = 10;

        TypedQuery<Book> jpql_query = entityManager.createQuery("" +
                        "SELECT b " +
                        "FROM Book b " +
                        "WHERE b.price > :value",
                Book.class)
                .setParameter("value", value);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cc_query = cb.createQuery(Book.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cc_query_root)
                .where(cb.gt(cc_query_root.get(Book_.price), cb.parameter(Integer.class, "value")));

        assertThat(entityManager.createQuery(cc_query)
                .setParameter("value", value)
                .getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getBooksWithMoreThanOneAuthors() {
        TypedQuery<Book> jpql_query = entityManager.createQuery("" +
                        "SELECT b " +
                        "FROM Book b " +
                        "WHERE size(b.authors) > 1",
                Book.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cc_query = cb.createQuery(Book.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cc_query_root)
                .where(cb.gt(cb.size(cc_query_root.get(Book_.authors)), 1));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void countBooksByGenre() {
        TypedQuery<Tuple> jpql_query =
                entityManager.createQuery("" +
                                "SELECT " +
                                "b.genre AS genre, count(b) AS count " +
                                "FROM Book b " +
                                "GROUP BY b.genre",
                        Tuple.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cc_query = cb.createTupleQuery();
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cb.tuple(
                cc_query_root.get(Book_.genre).alias("genre"),
                cb.count(cc_query_root).alias("count")))
                .groupBy(cc_query_root.get(Book_.genre));

        List<CountBooksByGenreTupleWrapper> jpql_query_results = jpql_query.getResultList()
                .stream()
                .map(CountBooksByGenreTupleWrapper::new)
                .collect(Collectors.toList());

        List<CountBooksByGenreTupleWrapper> cc_query_results = entityManager.createQuery(cc_query).getResultList()
                .stream()
                .map(CountBooksByGenreTupleWrapper::new)
                .collect(Collectors.toList());

        assertThat(cc_query_results).containsExactlyInAnyOrderElementsOf(jpql_query_results);
    }

    @Test
    void getGenresThatHaveMoreThanOneBook() {
        TypedQuery<WritingGenre> jpql_query = entityManager.createQuery("" +
                        "SELECT b.genre " +
                        "FROM Book b " +
                        "GROUP BY b.genre " +
                        "HAVING count(b.genre) > 1",
                WritingGenre.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<WritingGenre> cc_query = cb.createQuery(WritingGenre.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cc_query_root.get(Book_.genre))
                .groupBy(cc_query_root.get(Book_.genre))
                .having(cb.gt(cb.count(cc_query_root.get(Book_.genre)), 1));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getBooksByTitle() {
        String title = "Harry Potter";

        TypedQuery<Book> jpql_query =
                entityManager.createQuery("" +
                                "SELECT b " +
                                "FROM Book b " +
                                "WHERE b.title = :title",
                        Book.class)
                        .setParameter("title", title);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cc_query = cb.createQuery(Book.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        cc_query.select(cc_query_root)
                .where(cb.equal(cc_query_root.get(Book_.title), cb.parameter(String.class, "title")));

        assertThat(entityManager.createQuery(cc_query)
                .setParameter("title", title)
                .getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getBookstoresWithAtLeastOneBook() {
        TypedQuery<Bookstore> jpql_query_in =
                entityManager.createQuery("" +
                                "SELECT DISTINCT bookstore " +
                                "FROM Bookstore bookstore, IN(bookstore.books) books",
                        // note that without alias 'books' in won't work
                        // (org.hibernate.hql.internal.ast.QuerySyntaxException)
                        Bookstore.class);

        TypedQuery<Bookstore> jpql_query_join =
                entityManager.createQuery("" +
                                "SELECT DISTINCT bookstore " +
                                "FROM Bookstore bookstore JOIN bookstore.books",
                        Bookstore.class);


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bookstore> cc_query = cb.createQuery(Bookstore.class);
        Root<Bookstore> cc_query_root = cc_query.from(Bookstore.class);
        cc_query_root.join(Bookstore_.books);
        cc_query.select(cc_query_root).distinct(true);

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query_in.getResultList());

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query_join.getResultList());
    }

    @Test
    void getBookstoresWithMostExpensiveBook() {
        TypedQuery<Bookstore> jpql_query = entityManager.createQuery("" +
                        "SELECT book.bookstore " +
                        "FROM Book book " +
                        "WHERE book.price = (SELECT MAX(b.price) FROM Book b)",
                Bookstore.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bookstore> cc_query = cb.createQuery(Bookstore.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);
        cc_query_root.join(Book_.bookstore);

        Subquery<Integer> cc_max_subquery = cc_query.subquery(Integer.class);
        Root<Book> cc_max_subquery_root = cc_max_subquery.from(Book.class);
        cc_max_subquery.select(cb.max(cc_max_subquery_root.get(Book_.price)));

        cc_query.select(cc_query_root.get(Book_.bookstore))
                .where(cb.equal(cc_query_root.get(Book_.price), cc_max_subquery));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void findEntitlementForAnnouncement() {
        TypedQuery<Announcement> jpql_query =
                entityManager.createQuery("" +
                                "SELECT a " +
                                "FROM Announcement a " +
                                "WHERE 0 = (SELECT COUNT(e.e_id) FROM Entitlement e where e.announcementId = a.a_id)",
                        Announcement.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Announcement> cc_query = cb.createQuery(Announcement.class);
        Root<Announcement> cc_a_root = cc_query.from(Announcement.class);

        Subquery<Long> cc_cnt_sq = cc_query.subquery(Long.class);
        Root<Entitlement> cc_cnt_sq_root = cc_cnt_sq.from(Entitlement.class);
        cc_cnt_sq.select(cb.count(cc_cnt_sq_root.get("e_id")))
                .where(cb.equal(cc_cnt_sq_root.get("announcementId"), cc_a_root.get("a_id")));

        cc_query.select(cc_a_root).where(cb.equal(cc_cnt_sq, 0));

        System.out.println("RESULT SQL--->" + jpql_query.getResultList());
        System.out.println("RESULT JPQL===>" + entityManager.createQuery(cc_query).getResultList());

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .isEqualTo(jpql_query.getResultList());
    }

    @Test
    void findEntitlementForAnnouncementByDate() {
        TypedQuery<Announcement> jpql_query =
                entityManager.createQuery("" +
                                "SELECT a " +
                                "FROM Announcement a " +
                                "WHERE " +
                                " (YEAR(a.createdOn)=2019) AND (MONTH(a.createdOn)=6) AND (DAY(a.createdOn)=1) AND " +
                                "2 = (SELECT COUNT(e.e_id) FROM Entitlement e where e.announcementId = a.a_id)",
                        Announcement.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Announcement> cc_query = cb.createQuery(Announcement.class);
        Root<Announcement> cc_a_root = cc_query.from(Announcement.class);

        Expression<Integer> year = cb.function("year", Integer.class, cc_a_root.get("createdOn"));
        Expression<Integer> month = cb.function("month", Integer.class, cc_a_root.get("createdOn"));
        Expression<Integer> day = cb.function("day", Integer.class, cc_a_root.get("createdOn"));
        Predicate createdOn = cb.and(cb.equal(year, 2019), cb.equal(month, 6), cb.equal(day, 1));

        Subquery<Long> cc_cnt_sq = cc_query.subquery(Long.class);
        Root<Entitlement> cc_cnt_sq_root = cc_cnt_sq.from(Entitlement.class);
        cc_cnt_sq.select(cb.count(cc_cnt_sq_root.get("e_id")))
                .where(cb.equal(cc_cnt_sq_root.get("announcementId"), cc_a_root.get("a_id")));

        cc_query.select(cc_a_root).where(createdOn, cb.equal(cc_cnt_sq, 2));

        System.out.println("RESULT SQL--->" + jpql_query.getResultList());
        System.out.println("RESULT JPQL===>" + entityManager.createQuery(cc_query).getResultList());

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .isEqualTo(jpql_query.getResultList());
    }

    @Test
    void getBookstoresFromNewYork() {
        TypedQuery<Bookstore> jpql_query = entityManager.createQuery("" +
                        "SELECT bookstore " +
                        "FROM Bookstore bookstore " +
                        "WHERE bookstore.address.city = 'New York'",
                Bookstore.class);


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bookstore> cc_query = cb.createQuery(Bookstore.class);
        Root<Bookstore> cc_query_root = cc_query.from(Bookstore.class);

        cc_query.select(cc_query_root)
                .where(cb.equal(cc_query_root.get(Bookstore_.address).get(Address_.city), "New York"));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    /**
     * the easiest way:
     * SELECT DISTINCT book.BOOKSTORE_ID
     * FROM BOOK book
     * WHERE book.TITLE LIKE :title
     * <p>
     * we show how to reference a FROM expression of the parent query in the FROM clause
     * of a subquery
     */
    @Test
    void getBookstoresThatHaveTitle() {
        String title = "Harry Potter";

        TypedQuery<Bookstore> jpql_query = entityManager.createQuery("" +
                        "SELECT bookstore " +
                        "FROM Bookstore bookstore " +
                        "WHERE EXISTS " +
                        "(SELECT b " +
                        "FROM bookstore.books b " +
                        "WHERE b.title = :title)",
                Bookstore.class)
                .setParameter("title", title);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bookstore> cc_query = cb.createQuery(Bookstore.class);
        Root<Bookstore> cc_query_root = cc_query.from(Bookstore.class);

        Subquery<Book> cc_subquery = cc_query.subquery(Book.class);
        Root<Bookstore> cc_subquery_root = cc_subquery.correlate(cc_query_root); // reference parent's query 'FROM' expression in subquery 'FROM'
        Join<Bookstore, Book> book = cc_subquery_root.join(Bookstore_.books);
        cc_subquery.select(book)
                .where(cb.equal(book.get(Book_.title), cb.parameter(String.class, "title")));

        cc_query.select(cc_query_root)
                .where(cb.exists(cc_subquery));

        assertThat(entityManager.createQuery(cc_query)
                .setParameter("title", title)
                .getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    /**
     * the easiest way:
     * SELECT DISTINCT b.BOOKSTORE_ID
     * FROM AUTHOR author
     * JOIN BOOK_AUTHOR bookAuthor ON (bookAuthor.AUTHORS_ID = author.ID)
     * JOIN BOOK B on bookAuthor.BOOKS_ID = B.ID
     * WHERE author.NAME = :author
     * <p>
     * we show how to reference a JOIN expression of the parent query in the FROM clause
     * of a subquery
     */
    @Test
    void getBookstoresThatHaveAtLeastOneBookWrittenBy() {
        String author = "Joshua Bloch";

        TypedQuery<Bookstore> jpql_query = entityManager.createQuery(
                "SELECT bookstore " +
                        "FROM Bookstore bookstore JOIN bookstore.books book " +
                        "WHERE EXISTS (SELECT ath FROM book.authors ath WHERE ath.name = :author)",
                Bookstore.class)
                .setParameter("author", author);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bookstore> cc_query = cb.createQuery(Bookstore.class);
        Root<Bookstore> cc_query_root = cc_query.from(Bookstore.class);
        Join<Bookstore, Book> books = cc_query_root.join(Bookstore_.books);

        Subquery<Author> cc_subquery = cc_query.subquery(Author.class);
        Join<Bookstore, Book> cc_subquery_root = cc_subquery.correlate(books); // reference parent's query JOIN expression in subquery 'FROM'
        Join<Book, Author> authors = cc_subquery_root.join(Book_.authors);
        cc_subquery.select(authors)
                .where(cb.equal(authors.get(Author_.name), cb.parameter(String.class, "author")));

        cc_query.select(cc_query_root)
                .where(cb.exists(cc_subquery));

        assertThat(entityManager.createQuery(cc_query)
                .setParameter("author", author)
                .getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getBooksWithFetchedAuthors() {
        TypedQuery<Book> jpql_query = entityManager.createQuery("" +
                        "SELECT b " +
                        "FROM Book b JOIN FETCH b.authors",
                Book.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Book> cc_query = cb.createQuery(Book.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);
        cc_query_root.fetch(Book_.authors);
        cc_query.select(cc_query_root);

        List<Book> resultList = entityManager.createQuery(cc_query).getResultList();

        Preconditions.checkState(resultList.stream().map(Book::getAuthors).allMatch(Hibernate::isInitialized),
                "Not all book.authors are fully initialized!");

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

    @Test
    void getBookstoresWithCountBooksAndPriceAverage() {
        TypedQuery<BookstoreCountAVG> jpql_query = entityManager.createQuery("" +
                        "SELECT new BookstoreCountAVG(b.bookstore, count(b), avg(b.price)) " +
                        "FROM Book b " +
                        "GROUP BY b.bookstore",
                BookstoreCountAVG.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BookstoreCountAVG> cc_query = cb.createQuery(BookstoreCountAVG.class);
        Root<Book> cc_query_root = cc_query.from(Book.class);

        // BookstoreCountAVG must have exact constructor as multiselect clause
        cc_query.multiselect(cc_query_root.get(Book_.bookstore),
                cb.count(cc_query_root),
                cb.avg(cc_query_root.get(Book_.price)))
                .groupBy(cc_query_root.get(Book_.bookstore));

        assertThat(entityManager.createQuery(cc_query).getResultList())
                .containsExactlyInAnyOrderElementsOf(jpql_query.getResultList());
    }

}
