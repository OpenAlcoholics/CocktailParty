UPDATE ingredient
SET name = :entity.name, image_link = :entity.imageLink, notes = :entity.notes,
    alcohol_percentage = :entity.alcoholPercentage, generic_id = :entity.generic.id
WHERE id = :entity.id
