package cr.ac.una.eif509.demo.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "docker"})
@Order(1)
public class MongoSchemaInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    public MongoSchemaInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!mongoTemplate.collectionExists("bitacora_eventos")) {
            MongoJsonSchema schema = MongoJsonSchema.builder()
                .required("entidad", "accion", "usuario", "detalle", "fechaHora")
                .build();
            mongoTemplate.createCollection("bitacora_eventos",
                CollectionOptions.empty().validator(Validator.schema(schema)));
        }

        mongoTemplate.indexOps("bitacora_eventos")
                .ensureIndex(new Index().on("fechaHora", Sort.Direction.DESC));
        mongoTemplate.indexOps("bitacora_eventos")
                .ensureIndex(new Index().on("entidad", Sort.Direction.ASC).on("accion", Sort.Direction.ASC));
    }
}