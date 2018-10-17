INSERT INTO drinks(name, image_link, description, revision_date, notes, category_id, glass_id)
VALUES(:entity.name, :entity.imageLink, :entity.description, :now, :entity.notes, :entity.category.id, :entity.glass.id)
