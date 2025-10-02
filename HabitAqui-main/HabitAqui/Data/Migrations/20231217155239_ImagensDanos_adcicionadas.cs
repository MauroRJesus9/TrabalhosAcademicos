using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HabitAqui.Data.Migrations
{
    public partial class ImagensDanos_adcicionadas : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_HabitacoesFotos_Arrendamentos_ArrendamentosId",
                table: "HabitacoesFotos");

            migrationBuilder.DropIndex(
                name: "IX_HabitacoesFotos_ArrendamentosId",
                table: "HabitacoesFotos");

            migrationBuilder.DropColumn(
                name: "ArrendamentosId",
                table: "HabitacoesFotos");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "ArrendamentosId",
                table: "HabitacoesFotos",
                type: "int",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_HabitacoesFotos_ArrendamentosId",
                table: "HabitacoesFotos",
                column: "ArrendamentosId");

            migrationBuilder.AddForeignKey(
                name: "FK_HabitacoesFotos_Arrendamentos_ArrendamentosId",
                table: "HabitacoesFotos",
                column: "ArrendamentosId",
                principalTable: "Arrendamentos",
                principalColumn: "Id");
        }
    }
}
