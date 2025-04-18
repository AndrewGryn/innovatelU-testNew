package org.example;


import java.util.List;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DocumentManager {

    //був public map бо я наповнював колекцію для перевірки класу Main.java
    private final Map<String, Document> storage = new HashMap<>();

    public Document save(Document document) {

        // цей if можна би було занести в builder і скоротити код,але думаю так краще
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        Document isDocumentInStorage = storage.get(document.getId()); //для того щоб перевірити чи апдейт чи нове для часу

        Document docToSave = Document.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .created(isDocumentInStorage != null ? isDocumentInStorage.getCreated() : document.getCreated()) //якщо буде апдейт,щоб брало старий час
                .build();

        // Якщо б поміняв анотацію на @Builder(toBuilder = true) і не робив if а зразу в id
        /*  Document docToSave = document.toBuilder()
                .id(document.getId() != null ? document.getId() : UUID.randomUUID().toString())
                .created(isDocumentInStorage != null ? isDocumentInStorage.getCreated() : document.getCreated())
                .build();  */

        storage.put(docToSave.getId(), docToSave);
        return docToSave;
    }

    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> {
                    /*
                    тут буде фільтрація по request:
                    1.загальний if для кожного поля на перевірку null або empty для кожного поля,щоб навіть якщо
                    є тільки одне поле з п'яти,шукало документ по одному полю,н-д якщо немає title,то if не виконається                    і буде шукати
                    для нього і виконається н-д для content і підбере документ вже по контенту
                    2.всередині if є ще перевірка на правильність даних,якщо дані(поля) !null в request і якщо одне з полів
                    чи то title не співпадає чи то в content не буде такого контенту,не буде ріквеститись документ,якщо хоть одне хибне
                    3.в датах(4-5ий if) перевіряю чи дата створення документу не менша і не більша стосовно реквесту
                     */

                    if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
                        boolean titleMatches = request.getTitlePrefixes().stream()
                                .anyMatch(prefix -> doc.getTitle() != null && doc.getTitle().startsWith(prefix));
                        if (!titleMatches) return false;
                    }

                    if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
                        boolean contentMatches = request.getContainsContents().stream()
                                .anyMatch(content -> doc.getContent() != null && doc.getContent().contains(content));
                        if (!contentMatches) return false;
                    }
                    //можна добавити перевірку чи пустий автор в документі і якщо дані не цілісні і автор пустий то не видавати request
                /*    if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
                        if (doc.getAuthor() == null || !request.getAuthorIds().contains(doc.getAuthor().getId())) {
                            return false;
                        }
                    }  */


                    if (request.getCreatedFrom() != null &&
                            (doc.getCreated() == null || doc.getCreated().isBefore(request.getCreatedFrom()))) {
                        return false;
                    }

                    if (request.getCreatedTo() != null &&
                            (doc.getCreated() == null || doc.getCreated().isAfter(request.getCreatedTo()))) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}



