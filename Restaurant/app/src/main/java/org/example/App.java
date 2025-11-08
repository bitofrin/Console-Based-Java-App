package org.example;

import java.util.*;

class Menu {
    String nama;
    double harga;
    String kategori; // "Makanan" atau "Minuman"

    Menu(String nama, double harga, String kategori) {
        this.nama = nama;
        this.harga = harga;
        this.kategori = kategori;
    }
}

public class App {
    static Scanner input = new Scanner(System.in);
    static ArrayList<Menu> menuList = new ArrayList<>();

    public static void main(String[] args) {
        inisialisasiMenu();

        while (true) {
            System.out.println("\n=== APLIKASI RESTORAN ===");
            System.out.println("1. Menu Pelanggan");
            System.out.println("2. Menu Pemilik");
            System.out.println("3. Keluar");
            System.out.print("Pilih menu: ");
            String pilihan = input.nextLine().trim();

            switch (pilihan) {
                case "1":
                    menuPelanggan();
                    break;
                case "2":
                    menuPemilik();
                    break;
                case "3":
                    System.out.println("Terima kasih telah menggunakan aplikasi restoran!");
                    return;
                default:
                    System.out.println("Pilihan tidak valid! Silakan pilih 1-3.");
            }
        }
    }

    // -------------------- MENU DASAR --------------------
    static void inisialisasiMenu() {
        // Minimal 4 menu untuk setiap kategori seperti diminta
        menuList.add(new Menu("Kimchi Bokkeumbap", 35000, "Makanan"));
        menuList.add(new Menu("Bulgogi", 40000, "Makanan"));
        menuList.add(new Menu("Tteokbokki", 30000, "Makanan"));
        menuList.add(new Menu("Japchae", 38000, "Makanan"));
        menuList.add(new Menu("Samgyeopsal", 45000, "Makanan"));

        menuList.add(new Menu("Soju", 25000, "Minuman"));
        menuList.add(new Menu("Makgeolli", 20000, "Minuman"));
        menuList.add(new Menu("Sikhye", 10000, "Minuman"));
        menuList.add(new Menu("Bingsoo", 22000, "Minuman/Dessert"));
        menuList.add(new Menu("Iced Green Tea Latte", 18000, "Minuman"));
    }

    static void tampilkanMenuGrouped() {
        System.out.println("\n=== DAFTAR MENU RESTORAN ===");
        // Group by category and show continuous numbering
        int nomor = 1;
        Map<Integer, Menu> indexMap = new LinkedHashMap<>();
        // First Makanan
        System.out.println("\n-- Makanan --");
        for (Menu m : menuList) {
            if (m.kategori.toLowerCase().contains("makanan")) {
                System.out.printf("%2d. %-25s Rp%,10.0f%n", nomor, m.nama, m.harga);
                indexMap.put(nomor, m);
                nomor++;
            }
        }
        // Then Minuman (termasuk dessert minuman)
        System.out.println("\n-- Minuman/Dessert --");
        for (Menu m : menuList) {
            if (m.kategori.toLowerCase().contains("minum")) {
                System.out.printf("%2d. %-25s Rp%,10.0f%n", nomor, m.nama, m.harga);
                indexMap.put(nomor, m);
                nomor++;
            }
        }
    }

    // -------------------- MENU PELANGGAN --------------------
    static void menuPelanggan() {
        LinkedHashMap<Menu, Integer> pesanan = new LinkedHashMap<>();

        while (true) {
            tampilkanMenuGrouped();
            System.out.print("\nMasukkan nomor menu yang ingin dipesan (atau ketik 'selesai' untuk finish): ");
            String entry = input.nextLine().trim();

            if (entry.equalsIgnoreCase("selesai")) {
                break;
            }

            int no;
            try {
                no = Integer.parseInt(entry);
            } catch (NumberFormatException e) {
                System.out.println("Input tidak valid. Masukkan nomor menu atau 'selesai'.");
                continue;
            }

            // map nomor ke menu
            Menu chosen = nomorKeMenu(no);
            if (chosen == null) {
                System.out.println("Nomor menu tidak ditemukan. Coba lagi.");
                continue;
            }

            System.out.print("Jumlah yang dipesan: ");
            String jumlahStr = input.nextLine().trim();
            int jumlah;
            try {
                jumlah = Integer.parseInt(jumlahStr);
                if (jumlah <= 0) {
                    System.out.println("Jumlah harus > 0.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Jumlah tidak valid. Masukkan angka.");
                continue;
            }

            pesanan.put(chosen, pesanan.getOrDefault(chosen, 0) + jumlah);
            System.out.println(jumlah + " x " + chosen.nama + " ditambahkan ke pesanan.");
        }

        if (pesanan.isEmpty()) {
            System.out.println("Anda belum memesan apapun.");
            return;
        }

        cetakStruk(pesanan);
    }

    static Menu nomorKeMenu(int nomor) {
        // recreate numbering same as tampilkanMenuGrouped to map nomor ke menu
        int idx = 1;
        for (Menu m : menuList) {
            if (m.kategori.toLowerCase().contains("makanan")) {
                if (idx == nomor) return m;
                idx++;
            }
        }
        for (Menu m : menuList) {
            if (m.kategori.toLowerCase().contains("minum")) {
                if (idx == nomor) return m;
                idx++;
            }
        }
        return null;
    }

    // -------------------- CETAK STRUK --------------------
    static void cetakStruk(Map<Menu, Integer> pesanan) {
        double initialSubtotal = 0;
        for (Menu item : pesanan.keySet()) {
            int qty = pesanan.get(item);
            initialSubtotal += item.harga * qty;
        }

        boolean promoMinuman = initialSubtotal > 50000; // Beli1Gratis1 untuk minuman
        boolean diskon10 = initialSubtotal > 100000;    // Diskon 10% + tambahan minuman gratis

        // Apply B1G1 promo for drinks: billed qty = ceil(qty/2)
        double subtotalAfterPromo = 0;
        double promoSavingsFromB1G1 = 0;
        Map<Menu, Integer> billedQuantities = new LinkedHashMap<>();
        for (Menu item : pesanan.keySet()) {
            int qty = pesanan.get(item);
            int billed = qty;
            if (promoMinuman && item.kategori.toLowerCase().contains("minum")) {
                billed = (qty + 1) / 2; // ceil(qty/2)
                double saved = (qty - billed) * item.harga;
                promoSavingsFromB1G1 += saved;
            }
            billedQuantities.put(item, billed);
            subtotalAfterPromo += billed * item.harga;
        }

        // Jika subtotal awal > 100000, berikan 1 minuman gratis tambahan (pilih minuman termurah dari menu pesanan)
        double extraFreeDrinkSaving = 0;
        if (diskon10) {
            Menu cheapestOrderedDrink = null;
            for (Menu item : pesanan.keySet()) {
                if (item.kategori.toLowerCase().contains("minum")) {
                    if (cheapestOrderedDrink == null || item.harga < cheapestOrderedDrink.harga) {
                        cheapestOrderedDrink = item;
                    }
                }
            }
            if (cheapestOrderedDrink != null) {
                extraFreeDrinkSaving = cheapestOrderedDrink.harga;
                subtotalAfterPromo -= extraFreeDrinkSaving;
                if (subtotalAfterPromo < 0) subtotalAfterPromo = 0;
            }
        }

        // Diskon 10% jika eligible (dihitung dari subtotal setelah promo)
        double diskon = 0;
        if (diskon10) {
            diskon = subtotalAfterPromo * 0.1;
        }

        double subtotalAfterDiscount = subtotalAfterPromo - diskon;
        if (subtotalAfterDiscount < 0) subtotalAfterDiscount = 0;

        double pajak = subtotalAfterDiscount * 0.10; // Pajak 10% atas total setelah diskon/promo
        double layanan = 20000;
        double totalAkhir = subtotalAfterDiscount + pajak + layanan;

        // Cetak struk
        String border = "======================================";
        System.out.println("\n" + border);
        System.out.println("           RESTORAN K-FOOD           ");
        System.out.println(border);
        System.out.printf("%-25s %5s %12s%n", "Item", "Qty", "Subtotal");
        System.out.println("--------------------------------------");

        for (Menu item : pesanan.keySet()) {
            int origQty = pesanan.get(item);
            int billedQty = billedQuantities.get(item);
            double lineTotal = billedQty * item.harga;
            // If billedQty != origQty, show both
            if (billedQty != origQty) {
                System.out.printf("%-25s %2d(%d) Rp%10.0f%n", item.nama, origQty, billedQty, lineTotal);
            } else {
                System.out.printf("%-25s %5d Rp%10.0f%n", item.nama, origQty, lineTotal);
            }
        }

        System.out.println("--------------------------------------");
        System.out.printf("%-30s Rp%10.0f%n", "Subtotal (sebelum promo/diskon):", initialSubtotal);
        if (promoSavingsFromB1G1 > 0) {
            System.out.printf("%-30s Rp%10.0f%n", "Penghematan (B1G1 minuman):", promoSavingsFromB1G1);
        }
        if (extraFreeDrinkSaving > 0) {
            System.out.printf("%-30s Rp%10.0f%n", "Special (minuman gratis):", extraFreeDrinkSaving);
        }
        System.out.printf("%-30s Rp%10.0f%n", "Subtotal (setelah promo):", subtotalAfterPromo + diskon); // add diskon back to show before discount
        if (diskon > 0) {
            System.out.printf("%-30s Rp%10.0f%n", "Diskon 10%:", -diskon);
        }
        System.out.printf("%-30s Rp%10.0f%n", "Subtotal setelah diskon:", subtotalAfterDiscount);
        System.out.printf("%-30s Rp%10.0f%n", "Pajak 10%:", pajak);
        System.out.printf("%-30s Rp%10.0f%n", "Biaya Layanan:", layanan);
        System.out.println("--------------------------------------");
        System.out.printf("%-30s Rp%10.0f%n", "TOTAL BAYAR:", totalAkhir);
        System.out.println(border);
        System.out.println("       TERIMA KASIH! SILAHKAN DATANG KEMBALI       ");
        System.out.println(border + "\n");
    }

    // -------------------- MENU PEMILIK --------------------
    static void menuPemilik() {
        while (true) {
            System.out.println("\n=== MENU PEMILIK RESTORAN ===");
            System.out.println("1. Tambah Menu");
            System.out.println("2. Ubah Harga");
            System.out.println("3. Hapus Menu");
            System.out.println("4. Lihat Daftar Menu");
            System.out.println("5. Kembali");
            System.out.print("Pilih: ");
            String pilih = input.nextLine().trim();

            switch (pilih) {
                case "1":
                    tambahMenu();
                    break;
                case "2":
                    ubahHarga();
                    break;
                case "3":
                    hapusMenu();
                    break;
                case "4":
                    tampilkanMenuAllWithIndex();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Pilihan tidak valid!");
            }
        }
    }

    static void tambahMenu() {
        System.out.print("Nama menu baru: ");
        String nama = input.nextLine().trim();
        if (nama.isEmpty()) {
            System.out.println("Nama tidak boleh kosong.");
            return;
        }

        System.out.print("Harga: ");
        String hargaStr = input.nextLine().trim();
        double harga;
        try {
            harga = Double.parseDouble(hargaStr);
            if (harga <= 0) {
                System.out.println("Harga harus > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Harga tidak valid.");
            return;
        }

        System.out.print("Kategori (Makanan/Minuman): ");
        String kategori = input.nextLine().trim();
        if (!(kategori.equalsIgnoreCase("Makanan") || kategori.equalsIgnoreCase("Minuman") || kategori.toLowerCase().contains("minum"))) {
            System.out.println("Kategori tidak valid. Gunakan 'Makanan' atau 'Minuman'.");
            return;
        }

        menuList.add(new Menu(nama, harga, kategori));
        System.out.println("Menu berhasil ditambahkan!");
    }

    static void ubahHarga() {
        tampilkanMenuAllWithIndex();
        System.out.print("Masukkan nomor menu yang ingin diubah: ");
        String noStr = input.nextLine().trim();
        int no;
        try {
            no = Integer.parseInt(noStr);
        } catch (NumberFormatException e) {
            System.out.println("Nomor tidak valid.");
            return;
        }

        if (no < 1 || no > menuList.size()) {
            System.out.println("Nomor tidak valid.");
            return;
        }

        Menu item = menuList.get(no - 1);
        System.out.print("Yakin ingin mengubah harga " + item.nama + "? (Ya/Tidak): ");
        String konfirmasi = input.nextLine().trim();

        if (konfirmasi.equalsIgnoreCase("Ya")) {
            System.out.print("Masukkan harga baru: ");
            String hargaStr = input.nextLine().trim();
            try {
                double hargaBaru = Double.parseDouble(hargaStr);
                if (hargaBaru <= 0) {
                    System.out.println("Harga harus > 0.");
                    return;
                }
                item.harga = hargaBaru;
                System.out.println("Harga berhasil diubah.");
            } catch (NumberFormatException e) {
                System.out.println("Harga tidak valid.");
            }
        } else {
            System.out.println("Batal diubah.");
        }
    }

    static void hapusMenu() {
        tampilkanMenuAllWithIndex();
        System.out.print("Masukkan nomor menu yang ingin dihapus: ");
        String noStr = input.nextLine().trim();
        int no;
        try {
            no = Integer.parseInt(noStr);
        } catch (NumberFormatException e) {
            System.out.println("Nomor tidak valid.");
            return;
        }

        if (no < 1 || no > menuList.size()) {
            System.out.println("Nomor tidak valid.");
            return;
        }

        Menu item = menuList.get(no - 1);
        System.out.print("Yakin ingin menghapus " + item.nama + "? (Ya/Tidak): ");
        String konfirmasi = input.nextLine().trim();

        if (konfirmasi.equalsIgnoreCase("Ya")) {
            menuList.remove(no - 1);
            System.out.println("Menu berhasil dihapus.");
        } else {
            System.out.println("Batal dihapus.");
        }
    }

    static void tampilkanMenuAllWithIndex() {
        System.out.println("\n=== DAFTAR MENU (index) ===");
        for (int i = 0; i < menuList.size(); i++) {
            Menu m = menuList.get(i);
            System.out.printf("%2d. %-25s Rp%,10.0f   [%s]%n", (i + 1), m.nama, m.harga, m.kategori);
        }
    }
}

