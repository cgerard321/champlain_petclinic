package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LocalizedNameIndexes {
    private final ReactiveMongoTemplate template;

    public Mono<Void> ensureIndexes() {
        return Flux.concat(
                template.indexOps(Inventory.class).ensureIndex(nameIndex("inventory", "en", false)),
                template.indexOps(Inventory.class).ensureIndex(nameIndex("inventory", "fr", false)),
                template.indexOps(Product.class).ensureIndex(nameIndex("supply", "en", true)),
                template.indexOps(Product.class).ensureIndex(nameIndex("supply", "fr", true)))
                .then();
    }

    private Index nameIndex(String prefix, String language, boolean withinInventory) {
        String field = "name." + language;
        Index index = new Index();
        if (withinInventory) {
            index.on("inventoryId", Direction.ASC);
        }
        return index.on(field, Direction.ASC)
                .named(prefix + "_name_" + language + "_unique")
                .unique()
                .partial(PartialIndexFilter.of(new Document(field, new Document("$type", "string"))))
                .collation(Collation.of(language).strength(Collation.ComparisonLevel.secondary()));
    }
}
