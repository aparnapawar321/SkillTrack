# Course Import CSV Format

This document describes the expected CSV format for importing courses with modules into SkillTrack.

## CSV Structure

### Header Row
```csv
title,description,instructorId,instructorEmail,modules
```

### Data Rows
```csv
"Introduction to Java","Learn Java programming from scratch",1,instructor@example.com,"Getting Started:Introduction to Java basics:0|Variables and Data Types:Learn about variables:1|Control Flow:If statements and loops:2"
"Advanced Spring Boot","Master Spring Boot framework",2,john.doe@example.com,"Spring Basics:Introduction to Spring:0|Dependency Injection:Understanding DI:1"
```

## Column Descriptions

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| `title` | String | Yes | Course title (max 200 characters) |
| `description` | String | No | Course description (can be long text) |
| `instructorId` | Long | Yes* | Instructor's user ID |
| `instructorEmail` | String | Yes* | Instructor's email address |
| `modules` | String | No | Pipe-separated list of modules (see format below) |

*Note: Either `instructorId` OR `instructorEmail` must be provided.

## Modules Format

Modules are encoded as a pipe-separated (`|`) list, where each module has three colon-separated (`:`) fields:

```
Title:Content:OrderIndex|Title:Content:OrderIndex|...
```

### Module Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `title` | String | Yes | Module title (max 200 characters) |
| `content` | String | Yes | Module content (can be long text) |
| `orderIndex` | Integer | Yes | Display order (0-based) |

### Module Examples

**Single Module:**
```
"Introduction:Welcome to the course:0"
```

**Multiple Modules:**
```
"Module 1:First module content:0|Module 2:Second module content:1|Module 3:Third module content:2"
```

**Important Notes:**
- Enclose the entire modules field in double quotes
- Use pipe (`|`) to separate modules
- Use colon (`:`) to separate module fields
- If module content contains colons, only the first two colons are used as delimiters
- OrderIndex should be sequential starting from 0

## Complete Example CSV

```csv
title,description,instructorId,instructorEmail,modules
"Python Fundamentals","Complete Python course for beginners",5,python@skilltrack.com,"Introduction to Python:Learn Python basics and setup your environment:0|Variables and Types:Understanding data types and variables:1|Functions:Creating and using functions:2|OOP Basics:Introduction to object-oriented programming:3"
"Web Development","Full-stack web development",,webdev@skilltrack.com,"HTML Basics:Introduction to HTML structure:0|CSS Styling:Learn CSS for styling:1|JavaScript:Programming for the web:2"
"Data Science 101","Introduction to data science",7,,"Python for Data:Using Python in data science:0|Pandas Library:Data manipulation with pandas:1|Visualization:Creating charts and graphs:2"
```

## Error Handling

Failed imports are logged to the `course_import_failures` table with:
- Raw CSV data
- Error message
- Timestamp
- Row number
- Batch ID

Common errors:
- Invalid module format (missing colons or pipes)
- Invalid orderIndex (non-numeric)
- Missing required fields
- Instructor not found

## File Location

Place CSV files in the import directory configured in `application.yml`:
```yaml
app:
  batch:
    import:
      directory: ./data/import
```

Default filename: `courses.csv`

## Tips for Creating CSV Files

1. **Use a CSV editor** or spreadsheet software (Excel, Google Sheets)
2. **Enclose fields with special characters** (commas, quotes, pipes) in double quotes
3. **Test with a small file first** before importing large datasets
4. **Validate module format** - ensure proper colon and pipe placement
5. **Keep orderIndex sequential** - start from 0 and increment by 1
