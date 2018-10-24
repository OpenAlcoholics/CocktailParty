UPDATE ingredient_categories
SET name = :entity.name,
    description = :entity.description,
    image_link = :entity.imageLink,
    is_alcoholic = :entity.alcoholic
WHERE id = :entity.id
