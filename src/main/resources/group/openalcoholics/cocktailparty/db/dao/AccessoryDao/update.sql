UPDATE accessories
SET name = :entity.name, description = :entity.description, image_link = :entity.imageLink,
    category_id = :entity.category.id
WHERE id = :entity.id
