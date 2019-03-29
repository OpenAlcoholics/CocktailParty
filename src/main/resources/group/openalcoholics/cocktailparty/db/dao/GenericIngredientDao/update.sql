UPDATE generic_ingredient
SET name = :entity.name,
    description = :entity.description,
    image_link = :entity.imageLink,
    is_alcoholic = :entity.isAlcoholic
WHERE id = :entity.id
