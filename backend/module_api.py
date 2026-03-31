from flask import Flask, request, jsonify

app = Flask(__name__)

# Sample data to simulate a database
MODULES = [
    {"id": 1, "name": "Module A", "type": "Type 1", "status": "Active"},
    {"id": 2, "name": "Module B", "type": "Type 2", "status": "Inactive"},
    {"id": 3, "name": "Module C", "type": "Type 1", "status": "Active"},
    {"id": 4, "name": "Module D", "type": "Type 3", "status": "Inactive"},
    {"id": 5, "name": "Module E", "type": "Type 2", "status": "Active"}
]

@app.route('/modules', methods=['GET'])
def get_modules():
    """
    API endpoint to retrieve a list of modules with optional filtering and pagination.
    Query Parameters:
        - type: Filter by module type (optional)
        - status: Filter by module status (optional)
        - page: Page number for pagination (default: 1)
        - page_size: Number of items per page (default: 10)
    """
    # Get query parameters
    module_type = request.args.get('type')
    status = request.args.get('status')
    page = int(request.args.get('page', 1))
    page_size = int(request.args.get('page_size', 10))

    # Filter modules based on query parameters
    filtered_modules = MODULES
    if module_type:
        filtered_modules = [m for m in filtered_modules if m['type'] == module_type]
    if status:
        filtered_modules = [m for m in filtered_modules if m['status'] == status]

    # Implement pagination
    total_items = len(filtered_modules)
    start_index = (page - 1) * page_size
    end_index = start_index + page_size
    paginated_modules = filtered_modules[start_index:end_index]

    # Construct response
    response = {
        "total_items": total_items,
        "page": page,
        "page_size": page_size,
        "modules": paginated_modules
    }
    return jsonify(response)

if __name__ == '__main__':
    app.run(debug=True)