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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ScopusCsv {

  public final String doi;
  public final String issn;
  public final String issue;
  public final String journal;
  public final String page;
  public final String title;
  public final String volume;
  public final String year;
  public final List<ScopusCsv.Author> authors;

  private ScopusCsv(CSVRecord csv) {
    doi = getCsvField(csv, "DOI");
    issn = getCsvField(csv, "ISSN");
    issue = getCsvField(csv, "Issue");
    journal = getCsvField(csv, "Source title");
    String pageStart = getCsvField(csv, "Page start");
    String pageEnd = getCsvField(csv, "Page end");
    if (pageStart != null && pageEnd != null) {
      page = pageStart + "-" + pageEnd;
    } else if (pageStart != null) {
      page = pageStart;
    } else {
      page = null;
    }
    title = getCsvField(csv, "Title");
    volume = getCsvField(csv, "Volume");
    year = getCsvField(csv, "Year");
    List<ScopusCsv.Author> authorList = new ArrayList<>();
    String authorString = csv.get("Authors").trim();
    if (!authorString.startsWith("[")) { /* Simple check for "[No author name available]" */
      for (String author : csv.get("Authors").split(",")) {
        authorList.add(new ScopusCsv.Author(author));
      }
    }
    authors = Collections.unmodifiableList(authorList);
  }

  private String getCsvField(CSVRecord csv, String field) {
    if (!csv.isSet(field)) {
      return null;
    }
    String ret = csv.get(field).trim();
    if (ret.length() == 0) {
      return null;
    }
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ScopusCsv [");
    sb.append(" title: ");
    sb.append(title);
    sb.append(" doi: ");
    sb.append(doi);
    sb.append(" url: ");
    sb.append(" issue: ");
    sb.append(issue);
    sb.append(" volume: ");
    sb.append(volume);
    sb.append(" page: ");
    sb.append(page);
    sb.append(" journal: ");
    sb.append(journal);
    sb.append(" issn: ");
    sb.append(issn);
    sb.append("]");
    return sb.toString();
  }

  public static List<ScopusCsv> fromFile(File scopusCsv) throws IOException {
    byte[] csvbytes = Files.readAllBytes(scopusCsv.toPath());
    /* Remove Excel byte order mark. */
    if (csvbytes[0] == (byte)0xef && csvbytes[1] == (byte)0xbb && csvbytes[2] == (byte)0xbf) {
      csvbytes = Arrays.copyOfRange(csvbytes, 3, csvbytes.length);
    }
    String csvdata = new String(csvbytes, StandardCharsets.UTF_8);
    CSVFormat.Builder formatBuilder = CSVFormat.Builder.create(CSVFormat.RFC4180);
    formatBuilder.setHeader();
    formatBuilder.setSkipHeaderRecord(true);
    CSVParser parser = CSVParser.parse(csvdata, formatBuilder.build());
    List<ScopusCsv> ret = new ArrayList<>();
    for (CSVRecord csvRecord : parser) {
      ret.add(new ScopusCsv(csvRecord));
    }
    return Collections.unmodifiableList(ret);
  }

  public static class Author {
    public final String firstName;
    public final String lastName;

    private Author(String name) {
      name = name.trim();
      int idx = name.lastIndexOf(' ');
      if (idx < 0) {
        lastName = name;
        firstName = "";
      } else {
        firstName = name.substring(idx + 1);
        lastName = name.substring(0, idx);
      }
    }
  }
}
