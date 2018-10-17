UPDATE drinks
SET name = :entity.name, image_link = :entity.imageLink, description = :entity.description,
    revision_date = :now, notes = :entity.notes, category_id = :entity.category.id,
    glass_id = :entity.glass.id
WHERE id = :entity.id
