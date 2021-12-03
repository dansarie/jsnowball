/* Copyright (c) 2021 Marcus Dansarie

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program. If not, see <http://www.gnu.org/licenses/>. */

package se.dansarie.jsnowball.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.dansarie.jsnowball.model.Article.ArticleStatus;

public class TestSnowballState {

  private SnowballState state1;
  private SnowballState state2;
  private Author author1_1;
  private Author author1_2;
  private Author author1_3;
  private Author author2_1;
  private Author author2_2;
  private Author author2_3;
  private Article article1_1;
  private Article article1_2;
  private Article article1_3;
  private Article article2_1;
  private Article article2_2;
  private Journal journal1_1;
  private Journal journal2_1;
  private Tag tag1_1;
  private Tag tag1_2;
  private Tag tag2_1;

  @BeforeEach
  public void setup() {
    state1 = new SnowballState();
    state2 = new SnowballState();

    author1_1 = new Author(state1);
    author1_1.setFirstName("Josiah");
    author1_1.setLastName("Stinkney Carberry");
    author1_1.setOrgName("Brown University");
    author1_1.setOrcId("0000-0002-1825-0097");
    author1_2 = new Author(state1);
    author1_2.setFirstName("Author2");
    author1_2.setLastName("State1");
    author1_2.setOrgName("An organization");
    author1_3 = new Author(state1);
    author1_3.setFirstName("Author3");
    author1_3.setLastName("State1");
    author1_3.setOrgName("An organization");
    author2_1 = new Author(state2);
    author2_1.setFirstName("Author1");
    author2_1.setLastName("State2");
    author2_1.setOrgName("An organization");
    author2_2 = new Author(state2);
    author2_2.setFirstName("Author2");
    author2_2.setLastName("State2");
    author2_2.setOrgName("An organization");
    author2_3 = new Author(state2);
    author2_3.setFirstName("Author3");
    author2_3.setLastName("State2");
    author2_3.setOrgName("An organization");

    journal1_1 = new Journal(state1);
    journal1_1.setName("Journal1_1");
    journal1_1.setIssn("0000-1111");
    journal2_1 = new Journal(state2);
    journal2_1.setName("Journal2_1");
    journal2_1.setIssn("0000-2222");

    tag1_1 = new Tag(state1);
    tag1_2 = new Tag(state1);
    tag2_1 = new Tag(state2);

    article1_1 = new Article(state1);
    article1_1.setTitle("Article1_1");
    article1_1.setDoi("10.1000/181");
    article1_1.setIssue("123");
    article1_1.setMonth("12");
    article1_1.setPages("11");
    article1_1.setVolume("110");
    article1_1.setYear("1911");
    article1_1.addAuthor(author1_1);
    article1_1.addAuthor(author1_2);
    article1_1.addTag(tag1_1);
    article1_1.addTag(tag1_2);
    article1_2 = new Article(state1);
    article1_2.setTitle("Article1_2");
    article1_2.setDoi("10.1000/182");
    article1_2.setIssue("123");
    article1_2.setMonth("12");
    article1_2.setPages("12");
    article1_2.setVolume("120");
    article1_2.setYear("1912");
    article1_2.addAuthor(author1_1);
    article1_2.addTag(tag1_2);
    article1_2.setJournal(journal1_1);
    article1_3 = new Article(state1);
    article1_3.setTitle("Article1_3");
    article1_3.setDoi("10.1000/183");
    article1_3.setIssue("123");
    article1_3.setMonth("12");
    article1_3.setPages("12");
    article1_3.setVolume("130");
    article1_3.setYear("1913");
    article1_3.addAuthor(author1_1);
    article1_3.setJournal(journal1_1);
    article2_1 = new Article(state2);
    article2_1.setTitle("Article2_1");
    article2_1.setMonth("12");
    article2_1.setPages("21");
    article2_1.setVolume("210");
    article2_1.setYear("1921");
    article2_1.addAuthor(author2_1);
    article2_1.setJournal(journal2_1);
    article2_1.addTag(tag2_1);
    article2_2 = new Article(state2);
    article2_2.setTitle("Article2_2");
    article2_2.setMonth("12");
    article2_2.setPages("22");
    article2_2.setVolume("220");
    article2_2.setYear("1922");

  }

  @Test
  public void testAddAuthor() {
    int len = article1_1.getAuthors().size();
    article1_1.addAuthor(author1_1);
    assertEquals(len, article1_1.getAuthors().size());
    assertTrue(article1_1.getAuthors().contains(author1_1));
    assertFalse(article1_1.getAuthors().contains(author1_3));
    article1_1.addAuthor(author1_3);
    assertTrue(article1_1.getAuthors().contains(author1_3));
    assertThrows(IllegalArgumentException.class, () -> article1_1.addAuthor(author2_1));
  }

  @Test
  public void testAddReference() {
    assertEquals(0, article1_1.getReferences().size());
    article1_1.addReference(article1_2);
    assertEquals(1, article1_1.getReferences().size());
    assertEquals(article1_2, article1_1.getReferences().get(0));
    assertEquals(1, article1_2.getReferencesTo().size());
    assertEquals(article1_1, article1_2.getReferencesTo().get(0));
    assertThrows(IllegalArgumentException.class, () -> article1_1.addReference(article2_1));
    assertThrows(IllegalArgumentException.class, () -> article1_1.addReference(article1_1));
  }

  @Test
  public void testAddTag() {
    assertEquals(2, article1_1.getTags().size());
    article1_1.addTag(tag1_1);
    assertEquals(2, article1_1.getTags().size());
    assertEquals(1, article1_2.getTags().size());
    article1_2.addTag(tag1_1);
    assertEquals(2, article1_2.getTags().size());
    assertEquals(tag1_1, article1_2.getTags().get(0));
    assertThrows(IllegalArgumentException.class, () -> article1_1.addTag(tag2_1));
  }

  @Test
  public void testDistanceTo() {
    assertEquals(-1, article1_1.distanceTo(article1_2));
    assertEquals(-1, article1_1.distanceTo(article1_3));
    assertEquals(0, article1_1.distanceTo(article1_1));
    assertEquals(0, article1_1.distanceTo(article1_3, article1_2, article1_1));
    article1_1.addReference(article1_2);
    article1_2.addReference(article1_3);
    assertEquals(1, article1_1.distanceTo(article1_3, article1_2));
    assertEquals(2, article1_1.distanceTo(article1_3));
    assertThrows(IllegalArgumentException.class, () -> article1_1.distanceTo(article2_1));
    assertThrows(IllegalArgumentException.class, () ->
        article1_1.distanceTo(article1_2, article2_1));
  }

  @Test
  public void testGetDoi() {
    assertEquals("10.1000/181", article1_1.getDoi());
    assertEquals("10.1000/182", article1_2.getDoi());
    assertEquals("10.1000/183", article1_3.getDoi());
    assertEquals("", article2_1.getDoi());
    assertEquals("", article2_2.getDoi());
  }

  @Test
  public void testGetIssue() {
    assertEquals("123", article1_1.getIssue());
    assertEquals("123", article1_2.getIssue());
    assertEquals("123", article1_3.getIssue());
    assertEquals("", article2_1.getIssue());
    assertEquals("", article2_2.getIssue());
  }

  @Test
  public void testMergeAuthors() {
    assertThrows(IllegalArgumentException.class, () -> article1_1.merge(article1_1));
    assertThrows(IllegalArgumentException.class, () -> article1_1.merge(article2_1));
    article1_1.addReference(article1_3);
    article1_3.addReference(article1_1);
    article1_2.merge(article1_1);
    assertEquals(2, article1_2.getAuthors().size());
    assertTrue(article1_2.getAuthors().contains(author1_1));
    assertTrue(article1_2.getAuthors().contains(author1_2));
    assertEquals(2, article1_2.getTags().size());
    assertTrue(article1_2.getTags().contains(tag1_1));
    assertTrue(article1_2.getTags().contains(tag1_2));
    assertFalse(article1_3.getReferences().contains(article1_1));
    assertTrue(article1_2.getReferences().contains(article1_3));
    assertTrue(article1_3.getReferences().contains(article1_2));
  }

  @Test
  public void testRemoveAuthor() {
    article1_1.removeAuthor(author1_1);
    assertFalse(article1_1.getAuthors().contains(author1_1));
    assertTrue(article1_1.getAuthors().contains(author1_2));
    assertEquals(1, article1_1.getAuthors().size());
    assertThrows(IllegalArgumentException.class, () -> article1_1.removeAuthor(author1_1));
    article1_1.removeAuthor(author1_2);
    assertFalse(article1_1.getAuthors().contains(author1_2));
    assertEquals(0, article1_1.getAuthors().size());
    assertThrows(IllegalArgumentException.class, () -> article1_1.removeAuthor(author2_1));
  }

  @Test
  public void testRemoveReference() {
    assertThrows(IllegalArgumentException.class, () -> article1_1.removeReference(article1_2));
    assertThrows(IllegalArgumentException.class, () -> article1_1.removeReference(article2_1));
  }

  @Test
  public void testRemoveTag() {
    article1_1.removeTag(tag1_1);
    assertFalse(article1_1.getTags().contains(tag1_1));
    assertThrows(IllegalArgumentException.class, () -> article1_1.removeTag(tag1_1));
    assertThrows(IllegalArgumentException.class, () -> article1_1.removeTag(tag2_1));
  }

  @Test
  public void testStatus() {
    for (ArticleStatus status : ArticleStatus.values()) {
      article1_1.setStatus(status);
      assertEquals(status, article1_1.getStatus());
    }
    assertThrows(NullPointerException.class, () -> article1_1.setStatus(null));
  }

  @Test
  public void testStartSet() {
    article1_1.setStartSet(true);
    assertTrue(article1_1.inStartSet());
    article1_1.setStartSet(false);
    assertFalse(article1_1.inStartSet());
  }

  @Test
  public void testGetByDoi() {
    assertEquals(article1_1, Article.getByDoi(state1, "10.1000/181"));
    assertEquals(article1_2, Article.getByDoi(state1, "10.1000/182"));
    assertEquals(article1_3, Article.getByDoi(state1, "10.1000/183"));
    assertNull(Article.getByDoi(state1, "10.1000/184"));
  }

  @Test
  public void testGetByTitle() {
    assertEquals(article1_1, Article.getByTitle(state1, "Article1_1"));
    assertEquals(article1_2, Article.getByTitle(state1, "Article1_2"));
    assertEquals(article1_3, Article.getByTitle(state1, "Article1_3"));
    assertNull(Article.getByTitle(state2, "Article1_3"));
  }

  @Test
  public void testSerialization() {
    article1_1.addReference(article1_2);
    String json = state1.getSerializationProxy().toJson();
    SnowballState restored = SnowballState.fromJson(json);
    Article article1 = Article.getByTitle(restored, "Article1_1");
    Article article2 = Article.getByTitle(restored, "Article1_2");
    assertTrue(article1.getDoi().equals(article1_1.getDoi()));
    assertTrue(article1.getIssue().equals(article1_1.getIssue()));
    assertTrue(article2.getJournal().getName().equals(article1_2.getJournal().getName()));
    assertTrue(article2.getJournal().getIssn().equals(article1_2.getJournal().getIssn()));
    assertTrue(article1.getMonth().equals(article1_1.getMonth()));
    assertTrue(article1.getPages().equals(article1_1.getPages()));
    assertEquals(article1.inStartSet(), article1_1.inStartSet());
    assertEquals(article1.getStatus(), article1_1.getStatus());
    assertTrue(article1.getTitle().equals(article1_1.getTitle()));
    assertTrue(article1.getVolume().equals(article1_1.getVolume()));
    assertTrue(article1.getYear().equals(article1_1.getYear()));
    assertEquals(2, article1.getAuthors().size());
    assertTrue(article1.getTags().get(0).getName().equals(article1_1.getTags().get(0).getName()));
    assertEquals(article2, article1.getReferences().get(0));
    assertEquals(article1, article2.getReferencesTo().get(0));
  }
}
