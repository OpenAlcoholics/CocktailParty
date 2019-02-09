UPDATE ingredient
SET name = :entity.name, image_link = :entity.imageLink, notes = :entity.notes,
    alcohol_percentage = :entity.alcoholPercentage, category_id = :entity.category.id
WHERE id = :entity.id
