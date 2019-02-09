INSERT INTO ingredient(name, image_link, notes, alcohol_percentage, category_id)
VALUES(:entity.name, :entity.imageLink, :entity.notes, :entity.alcoholPercentage, :entity.category.id)
