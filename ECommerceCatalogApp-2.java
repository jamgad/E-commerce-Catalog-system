import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

// Simple class to hold info about one product
class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private int stock;

    public Product(int id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
}

// Simple class to hold a product and how many of it are in the cart
class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public String getProductName() { return product.getName(); }
    public String getCategory() { return product.getCategory(); }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return product.getPrice(); }
    public double getLineTotal() { return product.getPrice() * quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

// A very simple bar chart panel, drawn by hand using basic Graphics2D calls.
class BarChartPanel extends JPanel {
    private Map<String, Integer> data = new LinkedHashMap<String, Integer>();
    private String title;

    public BarChartPanel(String title) {
        this.title = title;
        setPreferredSize(new Dimension(400, 250));
        setBackground(Color.WHITE);
    }

    public void setData(Map<String, Integer> newData) {
        this.data = newData;
        repaint(); // ask Swing to redraw this panel with the new data
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.drawString(title, 10, 20);

        if (data.isEmpty()) {
            g2.drawString("No data yet.", 10, 40);
            return;
        }

        // find the biggest value so we can scale the bars to fit
        int maxValue = 1;
        for (int value : data.values()) {
            if (value > maxValue) {
                maxValue = value;
            }
        }

        int barWidth = 60;
        int gap = 30;
        int x = 40;
        int baseY = getHeight() - 40;
        int maxBarHeight = getHeight() - 80;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int barHeight = (int) (((double) entry.getValue() / maxValue) * maxBarHeight);

            g2.setColor(new Color(70, 130, 180)); // steel blue
            g2.fillRect(x, baseY - barHeight, barWidth, barHeight);

            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), x, baseY + 15);
            g2.drawString(String.valueOf(entry.getValue()), x + 15, baseY - barHeight - 5);

            x += barWidth + gap;
        }
    }
}

// Main class, the only public class, holds main() and builds the whole GUI
public class ECommerceCatalogApp extends JFrame {

    //data 
    private ArrayList<Product> productList = new ArrayList<Product>();
    private ArrayList<CartItem> cartList = new ArrayList<CartItem>();
    private int nextId = 1;
    private static final double TAX_RATE = 0.08;

    //browse tab components
    private DefaultTableModel browseModel;
    private JTable browseTable;
    private JComboBox<String> categoryFilterBox;
    private JTextField searchField;
    private JSpinner qtySpinner;

    //cart tab components
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;

    //admin tab components
    private JPasswordField adminPasswordField;
    private JTextField adminIdField;
    private JTextField adminNameField;
    private JComboBox<String> adminCategoryBox;
    private JTextField adminPriceField;
    private JTextField adminStockField;
    private DefaultTableModel adminModel;
    private JTable adminTable;
    private ArrayList<JComponent> adminLockedControls = new ArrayList<JComponent>();

    //dashboard tab components
    private BarChartPanel stockChartPanel;
    private JLabel totalValueLabel;
    private JLabel lowStockLabel;
    private JLabel topCategoryLabel;

    public ECommerceCatalogApp() {
        super("E-Commerce Product Catalog System");
        loadSampleProducts();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse & Search", buildBrowsePanel());
        tabs.addTab("Shopping Cart", buildCartPanel());
        tabs.addTab("Admin", buildAdminPanel());
        tabs.addTab("Dashboard", buildDashboardPanel());

        add(tabs);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        setMinimumSize(new Dimension(900, 600));
        setSize(1000, 650);
        setLocationRelativeTo(null);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        refreshBrowseTable();
        refreshCartTable();
        refreshAdminTable();
        refreshDashboard();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ECommerceCatalogApp().setVisible(true);
            }
        });
    }

    // adds a few starting products so the store isn't empty
    private void loadSampleProducts() {
        productList.add(new Product(nextId++, "Laptop", "Electronics", 799.99, 10));
        productList.add(new Product(nextId++, "Headphones", "Electronics", 49.99, 25));
        productList.add(new Product(nextId++, "Java Programming Book", "Books", 39.50, 15));
        productList.add(new Product(nextId++, "Novel - The Great Gatsby", "Books", 12.99, 30));
        productList.add(new Product(nextId++, "T-Shirt", "Clothing", 14.99, 50));
        productList.add(new Product(nextId++, "Jeans", "Clothing", 34.99, 20));
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private Product findProductById(int id) {
        for (Product p : productList) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    //BROWSE TAB 
    private JPanel buildBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        categoryFilterBox = new JComboBox<String>(new String[]{"All", "Electronics", "Books", "Clothing"});
        searchField = new JTextField(15);
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton addToCartBtn = new JButton("Add to Cart");

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Category:"));
        controls.add(categoryFilterBox);
        controls.add(new JLabel("Search:"));
        controls.add(searchField);
        controls.add(new JLabel("Qty:"));
        controls.add(qtySpinner);
        controls.add(addToCartBtn);

        String[] columnNames = {"ID", "Name", "Category", "Price", "Stock"};
        browseModel = new DefaultTableModel(columnNames, 0) {
            // makes the table read-only, since editing happens through the buttons/forms
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        browseTable = new JTable(browseModel);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(browseTable), BorderLayout.CENTER);

        categoryFilterBox.addActionListener(e -> refreshBrowseTable());

        // update the table as the user types in the search box
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshBrowseTable(); }
            public void removeUpdate(DocumentEvent e) { refreshBrowseTable(); }
            public void changedUpdate(DocumentEvent e) { refreshBrowseTable(); }
        });

        addToCartBtn.addActionListener(e -> handleAddToCart());

        return panel;
    }

    // rebuilds the browse table based on the category filter and search text
    private void refreshBrowseTable() {
        browseModel.setRowCount(0);
        String category = (String) categoryFilterBox.getSelectedItem();
        String search = searchField.getText().trim().toLowerCase();

        for (Product p : productList) {
            boolean matchesCategory = category.equals("All") || p.getCategory().equalsIgnoreCase(category);
            boolean matchesSearch = search.isEmpty()
                    || p.getName().toLowerCase().contains(search)
                    || String.valueOf(p.getId()).equals(search);

            if (matchesCategory && matchesSearch) {
                browseModel.addRow(new Object[]{
                        p.getId(), p.getName(), p.getCategory(),
                        String.format("$%.2f", p.getPrice()), p.getStock()
                });
            }
        }
    }

    private void handleAddToCart() {
        int row = browseTable.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a product from the table first.");
            return;
        }

        int id = (Integer) browseModel.getValueAt(row, 0);
        Product p = findProductById(id);
        if (p == null) {
            return;
        }

        int qty = (Integer) qtySpinner.getValue();
        if (qty > p.getStock()) {
            showMessage("Not enough stock. Only " + p.getStock() + " left.");
            return;
        }

        // if it's already in the cart, just add to the existing quantity
        boolean found = false;
        for (CartItem item : cartList) {
            if (item.getProduct().getId() == p.getId()) {
                item.setQuantity(item.getQuantity() + qty);
                found = true;
                break;
            }
        }
        if (!found) {
            cartList.add(new CartItem(p, qty));
        }

        p.setStock(p.getStock() - qty);

        refreshBrowseTable();
        refreshCartTable();
        refreshAdminTable();
        refreshDashboard();
    }

    //CART TAB
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"Product", "Quantity", "Unit Price", "Line Total"};
        cartModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cartTable = new JTable(cartModel);

        JButton removeBtn = new JButton("Remove Selected");
        JButton checkoutBtn = new JButton("Checkout");
        removeBtn.addActionListener(e -> handleRemoveFromCart());
        checkoutBtn.addActionListener(e -> handleCheckout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(removeBtn);
        buttonPanel.add(checkoutBtn);

        subtotalLabel = new JLabel("Subtotal: $0.00");
        taxLabel = new JLabel("Tax: $0.00");
        totalLabel = new JLabel("Total: $0.00");

        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(taxLabel);
        summaryPanel.add(totalLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        bottomPanel.add(summaryPanel, BorderLayout.EAST);

        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // rebuilds the cart table, row order matches cartList order bzabt
    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (CartItem item : cartList) {
            cartModel.addRow(new Object[]{
                    item.getProductName(), item.getQuantity(),
                    String.format("$%.2f", item.getUnitPrice()),
                    String.format("$%.2f", item.getLineTotal())
            });
        }
        updateCartSummary();
    }

    private void handleRemoveFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a cart item to remove.");
            return;
        }

        CartItem item = cartList.get(row);
        // give the stock back to the product since it's leaving the cart
        item.getProduct().setStock(item.getProduct().getStock() + item.getQuantity());
        cartList.remove(row);

        refreshBrowseTable();
        refreshCartTable();
        refreshAdminTable();
        refreshDashboard();
    }

    private void updateCartSummary() {
        double subtotal = 0;
        for (CartItem item : cartList) {
            subtotal += item.getLineTotal();
        }
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
        taxLabel.setText(String.format("Tax (%.0f%%): $%.2f", TAX_RATE * 100, tax));
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    // checkout: shows the final bill AND a short analysis of the order
    private void handleCheckout() {
        if (cartList.isEmpty()) {
            showMessage("Your cart is empty. Nothing to checkout.");
            return;
        }

        StringBuilder receipt = new StringBuilder();
        double subtotal = 0;

        // track spending per category so we can analyze it afterwards
        Map<String, Double> categorySpend = new LinkedHashMap<String, Double>();

        for (CartItem item : cartList) {
            double lineTotal = item.getLineTotal();
            subtotal += lineTotal;
            receipt.append(String.format("%-20s x%-3d $%.2f%n", item.getProductName(), item.getQuantity(), lineTotal));

            String cat = item.getCategory();
            double soFar = categorySpend.containsKey(cat) ? categorySpend.get(cat) : 0.0;
            categorySpend.put(cat, soFar + lineTotal);
        }

        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        receipt.append(String.format("%nSubtotal: $%.2f%n", subtotal));
        receipt.append(String.format("Tax: $%.2f%n", tax));
        receipt.append(String.format("Total: $%.2f%n%n", total));

        // find which category the customer spent the most in
        String topCategory = "";
        double topAmount = -1;
        for (String cat : categorySpend.keySet()) {
            if (categorySpend.get(cat) > topAmount) {
                topAmount = categorySpend.get(cat);
                topCategory = cat;
            }
        }

        receipt.append("Order Analysis:\n");
        receipt.append("- Highest spending category: ").append(topCategory)
                .append(" ($").append(String.format("%.2f", topAmount)).append(")\n");
        receipt.append("- Different items purchased: ").append(cartList.size()).append("\n");
        receipt.append("- Average price per item: $")
                .append(String.format("%.2f", subtotal / cartList.size())).append("\n");

        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);

        cartList.clear();
        refreshCartTable();
        refreshDashboard();
    }

    //ADMIN TAB
    private JPanel buildAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        adminPasswordField = new JPasswordField(10);
        JButton loginBtn = new JButton("Login");

        adminIdField = new JTextField(10);
        adminNameField = new JTextField(15);
        adminCategoryBox = new JComboBox<String>(new String[]{"Electronics", "Books", "Clothing"});
        adminPriceField = new JTextField(10);
        adminStockField = new JTextField(10);

        JButton addBtn = new JButton("Add Product");
        JButton editBtn = new JButton("Edit Product");
        JButton removeBtn = new JButton("Remove Product");

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // login row
        gbc.gridx = 0; gbc.gridy = row; topPanel.add(new JLabel("Admin password:"), gbc);
        gbc.gridx = 1; topPanel.add(adminPasswordField, gbc);
        gbc.gridx = 2; topPanel.add(loginBtn, gbc);
        row++;

        // id row
        gbc.gridx = 0; gbc.gridy = row; topPanel.add(new JLabel("Product ID (for edit/remove):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; topPanel.add(adminIdField, gbc);
        gbc.gridwidth = 1;
        row++;

        // name row
        gbc.gridx = 0; gbc.gridy = row; topPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; topPanel.add(adminNameField, gbc);
        gbc.gridwidth = 1;
        row++;

        // category row
        gbc.gridx = 0; gbc.gridy = row; topPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; topPanel.add(adminCategoryBox, gbc);
        gbc.gridwidth = 1;
        row++;

        // price row
        gbc.gridx = 0; gbc.gridy = row; topPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; topPanel.add(adminPriceField, gbc);
        gbc.gridwidth = 1;
        row++;

        // stock row
        gbc.gridx = 0; gbc.gridy = row; topPanel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; topPanel.add(adminStockField, gbc);
        gbc.gridwidth = 1;
        row++;

        // button row
        JPanel adminButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminButtonPanel.add(addBtn);
        adminButtonPanel.add(editBtn);
        adminButtonPanel.add(removeBtn);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        topPanel.add(adminButtonPanel, gbc);

        String[] columnNames = {"ID", "Name", "Category", "Price", "Stock"};
        adminModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        adminTable = new JTable(adminModel);

        // the table goes in CENTER so it actually expands and fills the rest of the tab
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(adminTable), BorderLayout.CENTER);

        // admin controls stay locked (disabled) until the correct password is entered
        adminLockedControls.add(adminIdField);
        adminLockedControls.add(adminNameField);
        adminLockedControls.add(adminCategoryBox);
        adminLockedControls.add(adminPriceField);
        adminLockedControls.add(adminStockField);
        adminLockedControls.add(addBtn);
        adminLockedControls.add(editBtn);
        adminLockedControls.add(removeBtn);
        for (JComponent c : adminLockedControls) {
            c.setEnabled(false);
        }

        loginBtn.addActionListener(e -> {
            String password = new String(adminPasswordField.getPassword());
            if (password.equals("admin123")) {
                for (JComponent c : adminLockedControls) {
                    c.setEnabled(true);
                }
                showMessage("Admin access granted.");
            } else {
                showMessage("Wrong password.");
            }
        });

        addBtn.addActionListener(e -> handleAdminAdd());
        editBtn.addActionListener(e -> handleAdminEdit());
        removeBtn.addActionListener(e -> handleAdminRemove());

        return panel;
    }

    private void refreshAdminTable() {
        adminModel.setRowCount(0);
        for (Product p : productList) {
            adminModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(),
                    String.format("$%.2f", p.getPrice()), p.getStock()
            });
        }
    }

    private void clearAdminForm() {
        adminIdField.setText("");
        adminNameField.setText("");
        adminCategoryBox.setSelectedIndex(0);
        adminPriceField.setText("");
        adminStockField.setText("");
    }

    private void handleAdminAdd() {
        try {
            String name = adminNameField.getText().trim();
            String category = (String) adminCategoryBox.getSelectedItem();
            double price = Double.parseDouble(adminPriceField.getText().trim());
            int stock = Integer.parseInt(adminStockField.getText().trim());

            if (name.isEmpty()) {
                showMessage("Please enter a product name.");
                return;
            }

            Product newProduct = new Product(nextId, name, category, price, stock);
            productList.add(newProduct);
            nextId++;

            showMessage("Product added with ID " + newProduct.getId());
            clearAdminForm();
            refreshBrowseTable();
            refreshAdminTable();
            refreshDashboard();
        } catch (NumberFormatException ex) {
            showMessage("Price must be a number and stock must be a whole number.");
        }
    }

    private void handleAdminEdit() {
        try {
            int id = Integer.parseInt(adminIdField.getText().trim());
            Product p = findProductById(id);
            if (p == null) {
                showMessage("No product with that ID.");
                return;
            }

            if (!adminNameField.getText().trim().isEmpty()) {
                p.setName(adminNameField.getText().trim());
            }
            p.setCategory((String) adminCategoryBox.getSelectedItem());
            if (!adminPriceField.getText().trim().isEmpty()) {
                p.setPrice(Double.parseDouble(adminPriceField.getText().trim()));
            }
            if (!adminStockField.getText().trim().isEmpty()) {
                p.setStock(Integer.parseInt(adminStockField.getText().trim()));
            }

            showMessage("Product updated.");
            clearAdminForm();
            refreshBrowseTable();
            refreshAdminTable();
            refreshDashboard();
        } catch (NumberFormatException ex) {
            showMessage("ID, price, and stock must be valid numbers.");
        }
    }

    private void handleAdminRemove() {
        try {
            int id = Integer.parseInt(adminIdField.getText().trim());
            Product p = findProductById(id);
            if (p == null) {
                showMessage("No product with that ID.");
                return;
            }

            productList.remove(p);
            showMessage("Product removed.");
            clearAdminForm();
            refreshBrowseTable();
            refreshAdminTable();
            refreshDashboard();
        } catch (NumberFormatException ex) {
            showMessage("Please enter a valid numeric ID.");
        }
    }

    //DASHBOARD TAB
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        stockChartPanel = new BarChartPanel("Stock Quantity by Category");

        totalValueLabel = new JLabel();
        lowStockLabel = new JLabel();
        topCategoryLabel = new JLabel();

        JPanel analysisPanel = new JPanel();
        analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.Y_AXIS));
        JLabel analysisTitle = new JLabel("Store Analysis:");
        analysisTitle.setFont(analysisTitle.getFont().deriveFont(Font.BOLD));
        analysisPanel.add(analysisTitle);
        analysisPanel.add(totalValueLabel);
        analysisPanel.add(lowStockLabel);
        analysisPanel.add(topCategoryLabel);

        panel.add(stockChartPanel, BorderLayout.CENTER);
        panel.add(analysisPanel, BorderLayout.SOUTH);

        return panel;
    }

    // recalculates the chart and the analysis text, called whenever inventory changes
    private void refreshDashboard() {
        Map<String, Integer> stockByCategory = new LinkedHashMap<String, Integer>();
        Map<String, Double> valueByCategory = new LinkedHashMap<String, Double>();

        for (Product p : productList) {
            int currentStock = stockByCategory.containsKey(p.getCategory()) ? stockByCategory.get(p.getCategory()) : 0;
            stockByCategory.put(p.getCategory(), currentStock + p.getStock());

            double currentValue = valueByCategory.containsKey(p.getCategory()) ? valueByCategory.get(p.getCategory()) : 0.0;
            valueByCategory.put(p.getCategory(), currentValue + (p.getPrice() * p.getStock()));
        }

        stockChartPanel.setData(stockByCategory);

        double totalValue = 0;
        for (double v : valueByCategory.values()) {
            totalValue += v;
        }
        totalValueLabel.setText(String.format("Total inventory value: $%.2f", totalValue));

        StringBuilder lowStock = new StringBuilder("Low stock items (below 5 units): ");
        boolean anyLow = false;
        for (Product p : productList) {
            if (p.getStock() < 5) {
                lowStock.append(p.getName()).append(" (").append(p.getStock()).append("), ");
                anyLow = true;
            }
        }
        if (!anyLow) {
            lowStock.append("None");
        }
        lowStockLabel.setText(lowStock.toString());

        String topCategory = "";
        double topValue = -1;
        for (String cat : valueByCategory.keySet()) {
            if (valueByCategory.get(cat) > topValue) {
                topValue = valueByCategory.get(cat);
                topCategory = cat;
            }
        }
        topCategoryLabel.setText("Category with highest inventory value: " + topCategory);
    }
}